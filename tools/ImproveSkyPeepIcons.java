import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Rebuild launcher artwork from the user's EXISTING SkyPeep aviator PNG.
 * No downloaded/generated character art, plugins or network dependencies.
 * Legacy sizes have a larger bird; Android 8+ uses the original aviator as
 * a separate masked adaptive-icon foreground against the same blue sky.
 */
public final class ImproveSkyPeepIcons {
    private static final int BLUE = 0x35AFF8;
    private static final String[] DENSITY =
        {"mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};
    private static final int[] SIZE = {48, 72, 96, 144, 192};

    private static BufferedImage sourceBird(BufferedImage src) {
        int width = src.getWidth(), height = src.getHeight();
        if (width != height || width < 128)
            throw new IllegalArgumentException("Expected original high-density SkyPeep icon");
        int bgR=(BLUE>>>16)&255, bgG=(BLUE>>>8)&255, bgB=BLUE&255;
        int minX=width, minY=height, maxX=-1, maxY=-1;
        for (int y=0;y<height;y++) for (int x=0;x<width;x++) {
            if (x < width*.17 || x > width*.83 ||
                y < height*.18 || y > height*.82) continue;
            int argb=src.getRGB(x,y);
            if ((argb>>>24)<100) continue;
            int diff=Math.max(Math.abs((argb>>>16&255)-bgR),
                Math.max(Math.abs((argb>>>8&255)-bgG),
                         Math.abs((argb&255)-bgB)));
            if (diff<=24) continue;
            minX=Math.min(minX,x); maxX=Math.max(maxX,x);
            minY=Math.min(minY,y); maxY=Math.max(maxY,y);
        }
        if (maxX-minX < width*.40 || maxY-minY < height*.35)
            throw new IllegalStateException("Original SkyPeep bird not found");
        minX=Math.max(0,minX-2); maxX=Math.min(width-1,maxX+2);
        minY=Math.max(0,minY-2); maxY=Math.min(height-1,maxY+2);
        BufferedImage bird=new BufferedImage(maxX-minX+1,maxY-minY+1,
                                              BufferedImage.TYPE_INT_ARGB);
        for(int y=minY;y<=maxY;y++) for(int x=minX;x<=maxX;x++) {
            int argb=src.getRGB(x,y);
            int diff=Math.max(Math.abs((argb>>>16&255)-bgR),
                Math.max(Math.abs((argb>>>8&255)-bgG),
                         Math.abs((argb&255)-bgB)));
            int matte=Math.max(0,Math.min(255,(diff-12)*255/30));
            int alpha=Math.min(argb>>>24,matte);
            bird.setRGB(x-minX,y-minY,(alpha<<24)|(argb&0xffffff));
        }
        return bird;
    }

    private static void quality(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                  RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                  RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static BufferedImage legacy(BufferedImage bird,int size) {
        BufferedImage target=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics=target.createGraphics();
        quality(graphics);
        graphics.setColor(new Color(BLUE));
        graphics.fillOval(0,0,size-1,size-1);
        graphics.setColor(new Color(12,68,91,145));
        graphics.setStroke(new BasicStroke(Math.max(.55f,size/220f)));
        graphics.drawOval(.5f,.5f,size-1.5f,size-1.5f);
        int w=Math.round(size*.755f),h=Math.round(w*bird.getHeight()/(float)bird.getWidth());
        graphics.drawImage(bird,(size-w)/2,(size-h)/2,w,h,null);
        graphics.dispose();
        if((target.getRGB(0,0)>>>24)!=0)
            throw new IllegalStateException("Launcher corner must remain transparent");
        return target;
    }

    private static BufferedImage adaptiveForeground(BufferedImage bird) {
        final int size=432;
        BufferedImage target=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics=target.createGraphics();
        quality(graphics);
        // Android launcher may mask an adaptive icon with any shape; keep the
        // entire bird in its central safe zone rather than painting a circle.
        int w=Math.round(size*.60f), h=Math.round(w*bird.getHeight()/(float)bird.getWidth());
        graphics.drawImage(bird,(size-w)/2,(size-h)/2,w,h,null);
        graphics.dispose();
        return target;
    }

    private static void png(BufferedImage image,Path path) throws Exception {
        Files.createDirectories(path.getParent());
        if (!ImageIO.write(image,"png",path.toFile()))
            throw new IllegalStateException("PNG encoder not available");
        BufferedImage checked=ImageIO.read(path.toFile());
        if(checked==null || checked.getWidth()!=image.getWidth() ||
                             checked.getHeight()!=image.getHeight())
            throw new IllegalStateException("Invalid generated icon "+path);
    }

    public static void main(String[] args) throws Exception {
        if(args.length!=2) throw new IllegalArgumentException(
            "Usage: java tools/ImproveSkyPeepIcons.java app/res build/generated/skypeep-res");
        Path source=Path.of(args[0]), dest=Path.of(args[1]);
        BufferedImage original=ImageIO.read(source.resolve(
            "mipmap-xxxhdpi/ic_launcher.png").toFile());
        if(original==null) throw new IllegalStateException("Missing original icon");
        BufferedImage bird=sourceBird(original);
        for(int i=0;i<DENSITY.length;i++){
            BufferedImage icon=legacy(bird,SIZE[i]);
            Path directory=dest.resolve("mipmap-"+DENSITY[i]);
            png(icon,directory.resolve("ic_launcher.png"));
            png(icon,directory.resolve("ic_launcher_round.png"));
            System.out.printf(Locale.ROOT,
                "PASS SkyPeep legacy icon %s: %dx%d%n",DENSITY[i],SIZE[i],SIZE[i]);
        }
        png(adaptiveForeground(bird),
            dest.resolve("drawable-nodpi/skypeep_aviator_foreground.png"));
        Path drawable=dest.resolve("drawable");
        Files.createDirectories(drawable);
        Files.writeString(drawable.resolve("skypeep_icon_foreground.xml"),
            "<bitmap xmlns:android=\"http://schemas.android.com/apk/res/android\" " +
            "android:src=\"@drawable/skypeep_aviator_foreground\" " +
            "android:gravity=\"fill\" android:filter=\"true\" />\n",
            StandardCharsets.UTF_8);
        Path colors=dest.resolve("values");
        Files.createDirectories(colors);
        Files.writeString(colors.resolve("skypeep_icon_colors.xml"),
            "<resources><color name=\"skypeep_icon_blue\">#35AFF8</color></resources>\n",
            StandardCharsets.UTF_8);
        Path adaptive=dest.resolve("mipmap-anydpi-v26");
        Files.createDirectories(adaptive);
        String xml="<adaptive-icon xmlns:android=\"http://schemas.android.com/apk/res/android\">" +
            "<background android:drawable=\"@color/skypeep_icon_blue\" />" +
            "<foreground android:drawable=\"@drawable/skypeep_icon_foreground\" />" +
            "</adaptive-icon>\n";
        Files.writeString(adaptive.resolve("ic_launcher.xml"),xml,StandardCharsets.UTF_8);
        Files.writeString(adaptive.resolve("ic_launcher_round.xml"),xml,StandardCharsets.UTF_8);
        System.out.println("PASS original aviator enlarged and Android adaptive mask staged");
    }
}
