import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Compare real emulator captures against the checked-in, user-provided
 * reference illustrations. Metrics are diagnostic and never imply 1:1 parity.
 * The viewport is center-fitted to the reference without distortion.
 */
class CompareSkyPeepScreens {
    private static final String[][] SCENES = {
        {"menu", "menu.png", "menu.jpg"},
        {"levels", "levels.png", "levels.jpg"},
        {"skins", "skins.png", "skins.jpg"},
        {"world-1", "world-1.png", "gameplay.jpg"}
    };
    private static BufferedImage load(Path path) throws IOException {
        if (!Files.isRegularFile(path)) throw new IOException("Missing screenshot/reference: " + path);
        BufferedImage img = ImageIO.read(path.toFile());
        if (img == null || img.getWidth() < 100 || img.getHeight() < 100)
            throw new IOException("Invalid screenshot/reference: " + path);
        return img;
    }
    private static BufferedImage fit(BufferedImage capture, int w, int h) {
        double scale = Math.min(capture.getWidth() / (double) w, capture.getHeight() / (double) h);
        int width = Math.min(capture.getWidth(), (int) Math.round(w * scale));
        int height = Math.min(capture.getHeight(), (int) Math.round(h * scale));
        int x = (capture.getWidth() - width) / 2;
        int y = (capture.getHeight() - height) / 2;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(capture, 0, 0, w, h, x, y, x + width, y + height, null);
        graphics.dispose();
        return img;
    }
    private static double meanAbsoluteError(BufferedImage actual, BufferedImage reference) {
        long difference = 0;
        long count = 0;
        for (int y = 0; y < reference.getHeight(); y += 4) {
            for (int x = 0; x < reference.getWidth(); x += 4) {
                int a = actual.getRGB(x, y);
                int b = reference.getRGB(x, y);
                difference += Math.abs(((a >> 16) & 255) - ((b >> 16) & 255));
                difference += Math.abs(((a >> 8) & 255) - ((b >> 8) & 255));
                difference += Math.abs((a & 255) - (b & 255));
                count += 3;
            }
        }
        return difference / (double) count;
    }
    // Sample a fixed, unoccluded illustrated island in Sky Start. The old
    // overwide pillar mask erased this region despite no bird or pillar being
    // present there. This is reference-to-real-emulator evidence, not an image.
    private static double regionMAE(
        BufferedImage actual, BufferedImage reference,
        double left, double top, double right, double bottom) {
        int x0=(int)(left*reference.getWidth());
        int x1=(int)(right*reference.getWidth());
        int y0=(int)(top*reference.getHeight());
        int y1=(int)(bottom*reference.getHeight());
        long diff=0,count=0;
        for(int y=y0;y<y1;y+=2)for(int x=x0;x<x1;x+=2){
            int a=actual.getRGB(x,y),b=reference.getRGB(x,y);
            for(int shift:new int[]{0,8,16}){
                diff+=Math.abs(((a>>>shift)&255)-((b>>>shift)&255));
                count++;
            }
        }
        return diff/(double)Math.max(1,count);
    }
    public static void main(String[] args) throws Exception {
        Path output = Path.of("smoke-results/visual-comparison.txt");
        StringBuilder report = new StringBuilder();
        report.append("SkyPeep reference-vs-emulator diagnostic (current Android CI build)\n")
              .append("MAE: mean absolute RGB channel difference (0=exact, 255=max)\n")
              .append("Captured pixels are center-fitted to illustration aspect ratio.\n")
              .append("Dynamic HUD, bird, gameplay and scaled text may differ legitimately.\n")
              .append("No automated 1:1 visual-parity claim is made from this metric.\n\n");
        for (String[] scene : SCENES) {
            BufferedImage screenshot = load(Path.of("smoke-results", scene[1]));
            BufferedImage reference = load(Path.of("assets/previews", scene[2]));
            BufferedImage adjusted = fit(screenshot, reference.getWidth(), reference.getHeight());
            double error = meanAbsoluteError(adjusted, reference);
            // Report three independent vertical regions to reveal where
            // screenshot UI and photographed artwork deviate. These are
            // DIAGNOSTICS, not automatic pixel-perfect certification.
            for (int band = 0; band < 3; band++) {
                double upper = band / 3.0;
                double lower = (band + 1) / 3.0;
                double detail = regionMAE(adjusted, reference,
                    0.08, upper, 0.92, lower);
                report.append(String.format(java.util.Locale.ROOT,
                    "%s | region-%d MAE=%.2f/255%n",
                    scene[0], band + 1, detail));
            }
            report.append(String.format(java.util.Locale.ROOT,
                "%s | MAE=%.2f/255 | screenshot=%dx%d | reference=%dx%d%n",
                scene[0], error, screenshot.getWidth(), screenshot.getHeight(),
                reference.getWidth(), reference.getHeight()));
            // Automated reference deviation budget for unchanged illustrated
            // screens. This is an early regression alarm, NOT a claim of 1:1.
            // Live gameplay poses/collectibles intentionally differ.
            double maxMae = "menu".equals(scene[0]) ? 9.5
                : "levels".equals(scene[0]) ? 15.0
                : "skins".equals(scene[0]) ? 12.5 : Double.POSITIVE_INFINITY;
            if(error > maxMae) throw new IOException(
                scene[0]+" illustration regressed: MAE="+error+
                ", permitted="+maxMae);
            if("world-1".equals(scene[0])){
                double islandError=regionMAE(adjusted,reference,.33,.23,.45,.35);
                report.append(String.format(java.util.Locale.ROOT,
                    "sky-start-original-midground-island | ROI MAE=%.2f/255%n",
                    islandError));
                // This metric is diagnostic until the revised masks are
                // verified against the actual emulator capture.
            }
        }
        // Compact score face moved. Dark Wind Gate and Moon Glow scenery are
        // deliberate. Require readable contrast across the actual score art
        // and numerals rather than demanding four outdated pale pixels.
        for (int world=1; world<=6; world++) {
            BufferedImage screenshot=load(Path.of("smoke-results","world-"+world+".png"));
            int minimum=255,maximum=0;
            long sum=0;
            int count=0;
            for(double fx=.43;fx<=.57;fx+=.007)
                for(double fy=.018;fy<=.091;fy+=.004) {
                    int x=(int)(screenshot.getWidth()*fx);
                    int y=(int)(screenshot.getHeight()*fy);
                    int rgb=screenshot.getRGB(x,y);
                    int lum=(((rgb>>>16)&255)*299+
                        ((rgb>>>8)&255)*587+(rgb&255)*114)/1000;
                    minimum=Math.min(minimum,lum);
                    maximum=Math.max(maximum,lum);
                    sum+=lum;
                    count++;
                }
            int average=(int)(sum/Math.max(1,count));
            report.append("world-").append(world)
                .append(" | compact-score ROI minimum=").append(minimum)
                .append(" maximum=").append(maximum)
                .append(" contrast=").append(maximum-minimum)
                .append(" average=").append(average).append("/255\n");
            if(maximum < 155 || maximum-minimum < 60)
                throw new IOException("Insufficient score contrast in world "+world+
                    " (min="+minimum+", max="+maximum+"). Check screenshot.");
        }
        Files.writeString(output, report.toString(), StandardCharsets.UTF_8);
        System.out.print(report);
    }
}
