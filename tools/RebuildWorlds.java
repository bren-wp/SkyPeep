import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

/**
 * Deterministically reconstructs the transparent flight corridor of each
 * existing SkyPeep 2D world from its art palette and unobstructed side scenery.
 * Removes baked-in HUD, bird, obstacle and banded inpainting artifacts.
 * Requires JDK 17 only: no internet, generated chat images or other assets.
 */
class RebuildWorlds {
    /** Soft organic removal shape for a baked bird, cloud or pillar cap. */
    private interface Region { double alpha(double x,double y,double feather); }
    private record Oval(double cx,double cy,double rx,double ry) implements Region {
        public double alpha(double x,double y,double feather) {
            double nx=(x-cx)/rx,ny=(y-cy)/ry;
            double signedDistance=(1.0-Math.hypot(nx,ny))*Math.min(rx,ry);
            return clamp(.5+signedDistance/feather);
        }
    }
    /** Round-ended mask for the actual articulated Wind Gate stone arches. */
    private record Capsule(double x1,double y1,double x2,double y2,
                           double radius) implements Region {
        public double alpha(double x,double y,double feather) {
            double dx=x2-x1,dy=y2-y1;
            double t=clamp(((x-x1)*dx+(y-y1)*dy)/
                Math.max(1.0,dx*dx+dy*dy));
            double distance=Math.hypot(x-(x1+t*dx),y-(y1+t*dy));
            return clamp(.5+(radius-distance)/feather);
        }
    }
    private record CoinCircle(double centerX,double centerY,double radius) {
        double alpha(double x,double y,double feather) {
            // Feather inside an expanded circle, not across the coin outline:
            // the full original 30px-radius dark ring is always replaced.
            return clamp(.5+(radius-Math.hypot(x-centerX,y-centerY))/feather);
        }
    }
    private record Area(double x1,double y1,double x2,double y2) implements Region {
        public double alpha(double x,double y,double feather) {
            double inside=Math.min(Math.min(x-x1,x2-x),Math.min(y-y1,y2-y));
            if(x>=x1&&x<=x2&&y>=y1&&y<=y2) return clamp(.5+inside/feather);
            double dx=Math.max(Math.max(x1-x,x-x2),0);
            double dy=Math.max(Math.max(y1-y,y-y2),0);
            return clamp(.5-Math.hypot(dx,dy)/feather);
        }
    }
    private static double clamp(double v){return Math.max(0,Math.min(1,v));}
    // 706 x 1254 world assets, with original artwork coordinates.
    private static final Region[][] MASKS = {
      // World 1-2: mask ONLY the photographed HUD, baked-in character,
      // motion streaks and fixed stone pillars. The older huge rectangles
      // erased real illustrated islands, mountains and clouds at mid-screen.
      // These source-pixel-space regions retain far more original scenery.
      {new Area(0,0,706,185),
       // Original top stone shaft with a separate irregular leafy lower cap.
       new Area(346,116,534,451),new Oval(439,468,145,105),
       // Original bird/scarf and speed streaks: erase elliptical silhouettes,
       // retaining nearby natural island and mountain pixels.
       new Oval(235,585,158,120),new Oval(60,629,137,70),
       // The original sky-start top pillar also carried a long fixed green
       // horizontal tail above the bird, visible in previous screenshots.
       new Oval(166,477,205,65),
       // The bottom post reaches the screen edge; soft rounded top avoids
       // the former horizontal horizon-size rectangle seam.
       new Oval(437,754,110,88),new Area(346,775,534,1280)},
      {new Area(0,0,706,185),
       new Area(309,147,532,448),new Oval(410,489,150,106),
       // A narrow overlapping plume is part of the fixed photograph.
       new Oval(273,539,77,54),
       // The photographed bird ends above the central cloud banks.
       // A taller old bird mask also erased genuine bright cloud pixels near
       // (280,750). Keep the live sprite separate while preserving clouds.
       new Oval(207,644,141,100),new Oval(51,685,113,58),
       // Preserve the original floating island, waterfall, cloud crowns and
       // stone bridge at x=50..320,y=750..830. A previous broad lower-plume
       // oval wrongly erased these real pixels. The photographed gold bottom
       // pillar begins at x≈337,y≈790 and has its own separate removal mask.
       new Oval(409,839,128,80),new Area(311,851,536,1280),
       new Oval(406,956,139,88)},
      // Crystal Drop: preserve the photographic moon, flying islands,
      // crystal mountain range, ruins and waterfalls behind real moving
      // pillar sprites. Wide rectangular masks used to erase them all.
      {new Area(0,0,706,185),
       new Area(376,60,583,365),new Oval(470,419,150,118),
       new Oval(366,304,25,38),new Oval(595,295,22,52),
       new Oval(261,616,142,101),new Oval(48,639,138,42),
       new Oval(477,758,141,104),new Area(392,803,590,1280),
       new Oval(595,905,26,66)},
      // Wind Gate: stone arches are offset and curved, not wide solid
      // rectangles. Capsules remove the baked arch/flags but preserve the
      // illustrated air currents, islands and waterfalls beside them.
      {new Area(0,0,706,177),
       new Capsule(310,190,350,295,80),new Oval(463,268,144,115),
       new Capsule(354,288,457,355,91),
       new Capsule(467,365,523,461,93),new Oval(526,515,116,91),
       new Oval(185,643,140,101),new Oval(48,685,140,45),
       new Capsule(440,766,362,888,96),
       new Capsule(362,880,360,1160,92),
       new Capsule(456,960,432,1160,99),
       new Area(281,1140,501,1280)},
      // Moon Glow: the former x=-20..618 / y=418..774 rectangle erased
      // clouds and near/far islands across almost the ENTIRE play corridor.
      // The baked bird, scarf and speed streak occupy only these curved
      // silhouettes, as in the original six matching gameplay illustrations.
      {new Area(0,0,706,207),new Area(233,103,580,528),
       new Oval(251,634,154,129),new Oval(85,663,201,78),
       new Area(219,695,591,1280)},
      // Sun Peak: retain the illustrated sunrise, floating waterfalls and
      // temple. Historical world-6 masks erased nearly the entire middle
      // scene: x=-20..618,y=418..774, including scenery well away from
      // the baked character and pillar silhouettes.
      {new Area(-100,-100,806,205),
       // Right-side fixed top column and ornamental lower cap.
       new Area(385,130,581,383),new Oval(481,399,130,115),
       // The baked bird and flight streaks. Keep the landscape outside them.
       new Oval(251,634,151,130),new Oval(85,663,200,78),
       // Lower fixed cap and the remainder of the right pillar shaft.
       new Oval(484,841,130,96),new Area(382,868,581,1280)}
    };
    private static final Area[][] COINS = {
      {}, {}, {}, {},
      {new Area(339,560,694,751)}, {}
    };
    // Original three Cloud Pass coin outlines: from actual source art at
    // 864x1536 then mapped to the verified 706x1254 build texture.
    private static final CoinCircle[] CLOUD_PASS_COINS={
        new CoinCircle(498.5,622.0,43.0),
        new CoinCircle(443.8,678.7,43.0),
        new CoinCircle(390.0,733.4,43.0)
    };
    private static final CoinCircle[] WIND_GATE_COINS={
        new CoinCircle(441.0,660.0,40.0),
        new CoinCircle(529.0,642.0,40.0),
        new CoinCircle(617.0,626.0,40.0)
    };
    private static CoinCircle[] photographedCoins(int world) {
        return world==2?CLOUD_PASS_COINS:
               world==4?WIND_GATE_COINS:new CoinCircle[0];
    }
    private static int mix(int a,int b,double t){return (int)Math.round(a+(b-a)*t);}
    /**
     * Small removed coins use the real scene's nearby BLUE sky samples,
     * never a scanline running through the neighboring photographed coin.
     * Excluding yellow outlines and averaging an annulus prevents stripes.
     * Upper and lower samples preserve the local vertical colour gradient.
     */
    private static int[] coinSkyColours(BufferedImage source,CoinCircle coin) {
        long[][] sums=new long[2][4];
        int cx=(int)Math.round(coin.centerX),cy=(int)Math.round(coin.centerY);
        for(int dy=-81;dy<=81;dy+=2)for(int dx=-81;dx<=81;dx+=2) {
            double radius=Math.hypot(dx,dy);
            if(radius<48 || radius>79)continue;
            int x=cx+dx,y=cy+dy;
            if(x<0||y<0||x>=source.getWidth()||y>=source.getHeight())continue;
            int rgb=source.getRGB(x,y);
            int r=(rgb>>>16)&255,g=(rgb>>>8)&255,b=rgb&255;
            // Reject coins, banners, masonry, greens and dark silhouettes.
            if(b<r+16 || g<r+14 || b<g-25 || g<110)continue;
            int side=dy<0?0:1;
            sums[side][0]+=r;
            sums[side][1]+=g;
            sums[side][2]+=b;
            sums[side][3]++;
        }
        int[] result=new int[2];
        for(int side=0;side<2;side++){
            if(sums[side][3]<40)throw new IllegalStateException(
                "Not enough unoccluded sky pixels around Cloud Pass coin");
            int r=(int)(sums[side][0]/sums[side][3]);
            int g=(int)(sums[side][1]/sums[side][3]);
            int b=(int)(sums[side][2]/sums[side][3]);
            result[side]=(r<<16)|(g<<8)|b;
        }
        return result;
    }
    private static int coinBackdrop(int[] upDown,CoinCircle coin,int y) {
        double factor=clamp(.5+(y-coin.centerY)/(coin.radius*2.));
        int upper=upDown[0],lower=upDown[1];
        int r=mix((upper>>>16)&255,(lower>>>16)&255,factor);
        int g=mix((upper>>>8)&255,(lower>>>8)&255,factor);
        int blue=mix(upper&255,lower&255,factor);
        return (r<<16)|(g<<8)|blue;
    }
    /**
     * Builds a gently varying sky colour field FROM THE ORIGINAL artwork
     * beside a removed screenshot obstacle. The former generic procedural
     * sky inserted artificial mountain/cloud stripes through source scenery.
     * Sample only blue sky outside every mask, use the channel-wise MEDIAN
     * to reject islands/stone/crystals, then smooth it vertically.
     *
     * Worlds 3/4 use source blue sky, while world 6 uses the original
     * orange sunset hues. Other illustrated pixels remain untouched.
     */
    private static boolean eligibleSky(int rgb,int world) {
        int red=(rgb>>>16)&255,green=(rgb>>>8)&255,blue=rgb&255;
        // Do not sample original structures, clouds, sun/moon or masonry.
        if(world==6)
            return red>=180 && green>=130 && blue>=50 &&
                   red-green>7 && green-blue>19;
        // Original Moon Glow night is dark indigo (green usually 40-95).
        // The daytime "green>105" rule rejected its entire genuine sky,
        // causing a missing donor field or borrowed pale cloud colours.
        if(world==5)
            return blue>red+35 && blue>green+18 &&
                   green>red+7 && green>23 && blue>80;
        // Waterfall foam/crystal highlights are cyan-green. Those objects
        // contaminated the old sky-donor grid, creating visible turquoise
        // horizontal bands beside reconstructed gates. Clear daytime SKY
        // is blue-dominant; do not borrow river/waterfall pixels.
        return blue>red+22 && green>red+12 &&
               blue>=green+3 && green>105;
    }

    private static boolean sourceSkyAt(BufferedImage source,int world,int x,int y) {
        if(x<0||y<0||x>=source.getWidth()||y>=source.getHeight())
            return false;
        int feather=world==6?36:32;
        for(int i=0;i<MASKS[world-1].length;i++)
            if(MASKS[world-1][i].alpha(x,y,world==6&&i==0?40:feather)>=.04)
                return false;
        return eligibleSky(source.getRGB(x,y),world);
    }

    private static int mixRgb(int first,int second,double factor) {
        int r=mix((first>>>16)&255,(second>>>16)&255,factor);
        int g=mix((first>>>8)&255,(second>>>8)&255,factor);
        int b=mix(first&255,second&255,factor);
        return (r<<16)|(g<<8)|b;
    }

    /**
     * Restore photographed sky with TWO-DIMENSIONAL source-derived samples.
     *
     * The earlier single RGB value per image row ignored the left-to-right
     * lighting of the original painting. Mask feathering then revealed hard
     * rectangular/oval boundaries against the unmodified side scenery.
     * A coarse 32px grid of nearby unmasked sky colours is interpolated
     * smoothly without touching any original image pixel outside a mask.
     * Sparse patches fall back to the vertically smoothed source median.
     */
    private static int[] sourceMatchedSky(
            BufferedImage source,int world) {
        int h=source.getHeight(),w=source.getWidth();
        int[][] sampled=new int[h][3];
        boolean[] known=new boolean[h];
        int[] r=new int[w],g=new int[w],b=new int[w];
        for(int y=0;y<h;y++){
            int count=0;
            for(int x=0;x<w;x+=4){
                if(!sourceSkyAt(source,world,x,y))continue;
                int rgb=source.getRGB(x,y);
                r[count]=(rgb>>>16)&255;
                g[count]=(rgb>>>8)&255;
                b[count]=rgb&255;
                count++;
            }
            if(count>=8){
                Arrays.sort(r,0,count);
                Arrays.sort(g,0,count);
                Arrays.sort(b,0,count);
                sampled[y][0]=r[count/2];
                sampled[y][1]=g[count/2];
                sampled[y][2]=b[count/2];
                known[y]=true;
            }
        }
        // Interpolate genuinely missing rows in both directions. Forward
        // fill alone produced sudden horizon bands when sky reappeared.
        int previous=-1,first=-1;
        for(int y=0;y<h;y++)if(known[y]){
            if(previous<0)first=y;
            else {
                for(int row=previous+1;row<y;row++){
                    double t=(row-previous)/(double)(y-previous);
                    for(int channel=0;channel<3;channel++)
                        sampled[row][channel]=mix(
                            sampled[previous][channel],sampled[y][channel],t);
                }
            }
            previous=y;
        }
        if(previous<0)throw new IllegalStateException(
            "No unmasked original sky colours found in world "+world);
        // The screenshot HUD masks the entire top 180..207 rows. The old
        // forward-fill cloned a single source row across that WHOLE area,
        // producing a sharp flat-colour band across the top of every world.
        // Extrapolate only the measured original sky gradient, with a
        // conservative slope bound to avoid inventing harsh colours.
        if(first>0){
            int second=first;
            while(second<h-1 && (second<first+85 || !known[second]))second++;
            if(!known[second])second=first;
            double span=Math.max(1,second-first);
            for(int row=0;row<first;row++)
                for(int channel=0;channel<3;channel++){
                    double gradient=Math.max(-.17,Math.min(.17,
                        (sampled[second][channel]-sampled[first][channel])/span));
                    sampled[row][channel]=(int)Math.max(0,Math.min(255,
                        Math.round(sampled[first][channel]+(row-first)*gradient)));
                }
        }
        int last=previous;
        // The lower source is sometimes obscured by clouds/illustrated caps.
        // A measured, bounded derivative is less conspicuous than a flat band.
        if(last<h-1){
            int earlier=last;
            while(earlier>0 && (earlier>last-85 || !known[earlier]))earlier--;
            if(!known[earlier])earlier=last;
            double span=Math.max(1,last-earlier);
            for(int row=last+1;row<h;row++)
                for(int channel=0;channel<3;channel++){
                    double gradient=Math.max(-.17,Math.min(.17,
                        (sampled[last][channel]-sampled[earlier][channel])/span));
                    sampled[row][channel]=(int)Math.max(0,Math.min(255,
                        Math.round(sampled[last][channel]+(row-last)*gradient)));
                }
        }

        int[] smooth=new int[h];
        for(int y=0;y<h;y++){
            long sr=0,sg=0,sb=0,total=0;
            for(int dy=-90;dy<=90;dy++){
                int sy=Math.max(0,Math.min(h-1,y+dy));
                int weight=91-Math.abs(dy);
                sr+=(long)sampled[sy][0]*weight;
                sg+=(long)sampled[sy][1]*weight;
                sb+=(long)sampled[sy][2]*weight;
                total+=weight;
            }
            smooth[y]=((int)(sr/total)<<16)|
                      ((int)(sg/total)<<8)|(int)(sb/total);
        }

        final int spacing=32;
        int cols=(w+spacing-1)/spacing+1;
        int rows=(h+spacing-1)/spacing+1;
        int[] nodes=new int[cols*rows];
        for(int row=0;row<rows;row++)for(int col=0;col<cols;col++){
            int cx=Math.min(w-1,col*spacing);
            int cy=Math.min(h-1,row*spacing);
            long sr=0,sg=0,sb=0,count=0;
            for(int yy=Math.max(0,cy-54);yy<=Math.min(h-1,cy+54);yy+=6)
                for(int xx=Math.max(0,cx-84);xx<=Math.min(w-1,cx+84);xx+=6){
                    if(!sourceSkyAt(source,world,xx,yy))continue;
                    int rgb=source.getRGB(xx,yy);
                    sr+=(rgb>>>16)&255;
                    sg+=(rgb>>>8)&255;
                    sb+=rgb&255;
                    count++;
                }
            int base=smooth[cy],colour=base;
            if(count>=12){
                int averaged=((int)(sr/count)<<16)|
                             ((int)(sg/count)<<8)|(int)(sb/count);
                // Avoid sampling a nearby unusually bright cloud or ridge
                // as a completely different sky colour inside the mask.
                int result=0;
                for(int shift:new int[]{0,8,16}){
                    int v=(averaged>>>shift)&255,bv=(base>>>shift)&255;
                    result|=(Math.max(bv-42,Math.min(bv+42,v))<<shift);
                }
                colour=mixRgb(base,result,.85);
            }
            nodes[row*cols+col]=colour;
        }
        int[] field=new int[w*h];
        for(int y=0;y<h;y++){
            int y0=Math.min(rows-2,y/spacing);
            double fy=(y-y0*spacing)/(double)spacing;
            for(int x=0;x<w;x++){
                int x0=Math.min(cols-2,x/spacing);
                double fx=(x-x0*spacing)/(double)spacing;
                int a=mixRgb(nodes[y0*cols+x0],
                             nodes[y0*cols+x0+1],fx);
                int c=mixRgb(nodes[(y0+1)*cols+x0],
                             nodes[(y0+1)*cols+x0+1],fx);
                field[y*w+x]=mixRgb(a,c,fy);
            }
        }
        return field;
    }

    /**
     * Colour-constrain synthetic sky beside intact source sky at *each*
     * removed obstacle boundary. The previous broad smooth donor grid did
     * not match the original painted sky at its perimeter, causing long
     * teal/blue stripe outlines around rectangular and capsule masks.
     *
     * Donors come strictly from unmasked SKY in the same original JPG. Four
     * directional nearest-source indices are O(width*height), with no per-
     * pixel allocation, external service or new graphical illustration.
     * Only the replacement field is adjusted; output keeps all pixels with
     * zero mask opacity 100% identical to the original source.
     */
    private static int[] boundaryMatchedSky(
            BufferedImage source,int world,int[] field) {
        final int w=source.getWidth(),h=source.getHeight(),n=w*h;
        if(field.length!=n)throw new IllegalArgumentException("Bad source sky dimensions");
        byte[] known=new byte[n];
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)
            known[y*w+x]=(byte)(sourceSkyAt(source,world,x,y)?1:0);
        int[] left=new int[n],right=new int[n],above=new int[n],below=new int[n];
        for(int y=0;y<h;y++) {
            int last=-1;
            for(int x=0;x<w;x++){
                int id=y*w+x;
                if(known[id]!=0)last=id;
                left[id]=last;
            }
            last=-1;
            for(int x=w-1;x>=0;x--){
                int id=y*w+x;
                if(known[id]!=0)last=id;
                right[id]=last;
            }
        }
        for(int x=0;x<w;x++){
            int last=-1;
            for(int y=0;y<h;y++){
                int id=y*w+x;
                if(known[id]!=0)last=id;
                above[id]=last;
            }
            last=-1;
            for(int y=h-1;y>=0;y--){
                int id=y*w+x;
                if(known[id]!=0)last=id;
                below[id]=last;
            }
        }
        final int maxDistance=204;
        // Nearest-source hue is dominant near the feather edge, but fades
        // smoothly into the 2D source-derived field deep inside a large
        // removed bird/pillar footprint.
        for(int y=0;y<h;y++)for(int x=0;x<w;x++) {
            int id=y*w+x;
            if(known[id]!=0)continue;
            double sr=0,sg=0,sb=0,weights=0;
            int nearest=maxDistance+1;
            for(int direction=0;direction<4;direction++) {
                int candidate=switch(direction) {
                    case 0 -> left[id];
                    case 1 -> right[id];
                    case 2 -> above[id];
                    default -> below[id];
                };
                if(candidate<0)continue;
                int d=(direction<2)?Math.abs(x-candidate%w):
                      Math.abs(y-candidate/w);
                if(d==0 || d>maxDistance)continue;
                nearest=Math.min(nearest,d);
                double weight=1.0/((d+14.0)*(d+14.0));
                int rgb=source.getRGB(candidate%w,candidate/w);
                sr+=((rgb>>>16)&255)*weight;
                sg+=((rgb>>>8)&255)*weight;
                sb+=(rgb&255)*weight;
                weights+=weight;
            }
            if(weights==0)continue;
            int originalField=field[id];
            int r=(int)Math.round(sr/weights);
            int g=(int)Math.round(sg/weights);
            int b=(int)Math.round(sb/weights);
            int donor=(Math.max(0,Math.min(255,r))<<16)|
                      (Math.max(0,Math.min(255,g))<<8)|
                       Math.max(0,Math.min(255,b));
            double blend=.83*Math.pow(1.0-nearest/(maxDistance+1.0),1.4);
            field[id]=mixRgb(originalField,donor,blend);
        }
        // Nearest donor rows can change abruptly where an original cloud or
        // waterfall begins. Such changes are authentic *donor* pixels but
        // should never create artificial scanline stripes inside a removed
        // screenshot object. Bound only the replacement field's vertical
        // per-channel derivative; all unmasked original JPG pixels are
        // still preserved verbatim in rebuild().
        final int MAX_SKY_ROW_JUMP=11;
        for(int x=0;x<w;x++)for(int y=1;y<h;y++) {
            int i=y*w+x,p=(y-1)*w+x;
            // The field is diagnostic/used only inside masks; constrain
            // even across known-sky to masked-sky transitions. The original
            // unmasked JPG remains drawn untouched in rebuild(). Skipping
            // these transitions previously left a new world-3 band at y=515.
            int rgb=field[i],aboveRgb=field[p],bounded=0;
            for(int shift:new int[]{0,8,16}) {
                int value=(rgb>>>shift)&255;
                int parent=(aboveRgb>>>shift)&255;
                bounded|=(Math.max(parent-MAX_SKY_ROW_JUMP,
                    Math.min(parent+MAX_SKY_ROW_JUMP,value))<<shift);
            }
            field[i]=bounded;
        }
        return field;
    }

    /** Standalone, offline regression for source-derived 2D sky gradients. */
    private static void verifySkyReconstruction() {
        int w=706,h=1254;
        BufferedImage input=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int red=25+x/12,green=143+x/14,blue=223+x/33;
            input.setRGB(x,y,(red<<16)|(green<<8)|blue);
        }
        int[] field=sourceMatchedSky(input,3);
        int left=(field[620*w+45]>>>16)&255;
        int right=(field[620*w+665]>>>16)&255;
        if(right-left<20)throw new IllegalStateException(
            "Regression: horizontal original-sky lighting was flattened");
        for(int y=100;y<h-2;y+=39)
            for(int x=16;x<w-2;x+=39) {
                int one=field[y*w+x],two=field[y*w+x+1];
                for(int shift:new int[]{0,8,16})
                    if(Math.abs(((one>>>shift)&255)-((two>>>shift)&255))>3)
                        throw new IllegalStateException(
                            "Regression: vertical reconstruction edge at "+x+","+y);
            }
        // The original Sky Start and Cloud Drift used procedural replacement
        // colours despite source sky being available at their mask edges.
        // Exercise both real reconstruction paths without altering source art.
        for(int world:new int[]{1,2}) {
            int[] rebuilt=sourceMatchedSky(input,world);
            // Sample clear source sky on both sides of the baked pillars,
            // not the midflight bird/motion masks around row 620.
            int lo=(rebuilt[300*w+45]>>>16)&255;
            int hi=(rebuilt[300*w+665]>>>16)&255;
            if(hi-lo<20)throw new IllegalStateException(
                "Regression: world "+world+" replacement lost horizontal source lighting");
        }
        // Genuine Moon Glow sky is dark indigo. The previous daytime
        // green>105 filter returned zero or wrong sky donors, which made
        // broad reconstructed areas unnaturally bright and featureless.
        BufferedImage night=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<h;y++)for(int x=0;x<w;x++) {
            int red=19+x/24,green=42+x/27,blue=120+x/10;
            night.setRGB(x,y,(red<<16)|(green<<8)|blue);
        }
        int[] moon=boundaryMatchedSky(night,5,sourceMatchedSky(night,5));
        int dark=moon[620*w+220];
        int moonBlue=dark&255,moonRed=(dark>>>16)&255;
        if(moonBlue<110 || moonBlue>205 || moonBlue-moonRed<45)
            throw new IllegalStateException(
                "Moon Glow original indigo source field was flattened/brightened");
        int[] stitched=boundaryMatchedSky(input,3,sourceMatchedSky(input,3));
        for(int y=250;y<950;y+=53)for(int x=35;x<680;x+=53) {
            int leftRgb=stitched[y*w+x],rightRgb=stitched[y*w+x+1];
            for(int shift:new int[]{0,8,16})
                if(Math.abs(((leftRgb>>>shift)&255)-
                            ((rightRgb>>>shift)&255))>16)
                    throw new IllegalStateException(
                        "Boundary-stitched source sky band at "+x+","+y);
        }
        System.out.println("PASS: original-source night sky, 2D continuity and seam-free boundary donors");
    }
    private static void rebuild(
            Path file,int world,Path sunPeakReference) throws Exception {
        BufferedImage original;
        if(world==6) {
            // Use the user's unmodified Sun Peak painting instead of the
            // already banded/inpainted former bg_6 source. All graphics
            // are local and tracked in Git; no network/AI asset generation.
            BufferedImage reference=ImageIO.read(sunPeakReference.toFile());
            if(reference==null || reference.getWidth()!=520 ||
               reference.getHeight()!=924)
                throw new IOException("Unexpected original Sun Peak reference: "+
                    sunPeakReference);
            original=new BufferedImage(706,1254,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=original.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(reference,0,0,706,1254,null);
            g.dispose();
        } else {
            original=ImageIO.read(file.toFile());
        }
        if(original==null)throw new IOException("Unreadable SkyPeep artwork: "+file);
        int w=original.getWidth(),h=original.getHeight();
        if(w!=706||h!=1254)throw new IOException("Unexpected art size: "+w+"x"+h);
        if(world==2){
            // The bottom-pillar cap has its own stable mask, independent of
            // the removed broad plume oval. Assert its geometry, not an
            // assumed input-pixel colour: packed source artwork and a
            // resized reference screenshot do not have identical RGBs.
            // (x=400,y=830) is well inside the cap mask; near its left edge
            // the historic cap-overlap region must also remain covered.
            for(int[] point:new int[][]{{400,830},{345,810}}){
                if(MASKS[1][6].alpha(point[0],point[1],27)<.99)
                    throw new IOException(
                        "Cloud Pass separate pillar-cap mask no longer covers "
                        +point[0]+","+point[1]);
            }
        }
        if(world==1){
            // The outer dark bottom rim of the original baked-in pillar spans
            // x≈350..535 around source y=525. Erase it completely so it
            // cannot appear stationary beneath the moving pillar sprite.
            for(int x:new int[]{350,535})
                if(MASKS[0][2].alpha(x,525,27)<.99)
                    throw new IOException("Sky Start fixed-pillar rim leaked: x="+x);
        }
        if(world==3 || world==4){
            // Baked screenshot obstacles must be replaced by actual moving
            // LibGDX pillar sprites. Verify the centres of their fixed
            // silhouettes are always covered by the corresponding masks.
            int[][] fixedObjects=world==3?
                new int[][]{{470,419},{260,616},{477,875}}:
                new int[][]{{455,335},{185,643},{365,990}};
            for(int[] point:fixedObjects){
                double opacity=0;
                for(Region mask:MASKS[world-1])
                    opacity=Math.max(opacity,mask.alpha(point[0],point[1],32));
                if(opacity<.98)throw new IOException(
                    "Photographed world-"+world+" game object was not removed at "+
                    point[0]+","+point[1]+": "+opacity);
            }
            for(CoinCircle coin:photographedCoins(world))
                if(coin.alpha(coin.centerX,coin.centerY,17)<.99)
                    throw new IOException("A fixed reference coin remained in world "+world);
        }
        if(world==6){
            // Test artwork integrity BEFORE JPEG compression. These are real
            // landscape landmarks on the original 706x1254 Sun Peak source,
            // and none is part of a moving bird, pillar or HUD element.
            // Prior broad replacement areas erased their original colours.
            final int[][] protectedLandmarks={
                {319,311}, // illustrated sun centre
                {80,385},  // left landscape/island
                {635,534}, // distant sunlit temple / waterfall skyline
                {645,385}, // right floating islands
                {95,1000}  // lower-left waterfall/scenery
            };
            for(int[] point:protectedLandmarks){
                double amount=0;
                for(int mask=0;mask<MASKS[5].length;mask++)
                    amount=Math.max(amount,MASKS[5][mask].alpha(
                        point[0],point[1],mask==0?40:36));
                if(amount>0.001)throw new IOException(
                    "Sun Peak illustrated landmark in mask: "+
                    point[0]+","+point[1]+", alpha="+amount);
            }
            final int[][] bakedObjects={
                {475,300}, // upper fixed stone pillar
                {481,399}, // upper cap
                {251,634}, // photographed original bird
                {85,650},  // baked flight streak
                {484,841}, // lower fixed pillar cap
                {483,1075} // lower shaft
            };
            for(int[] point:bakedObjects){
                double amount=0;
                for(int mask=0;mask<MASKS[5].length;mask++)
                    amount=Math.max(amount,MASKS[5][mask].alpha(
                        point[0],point[1],mask==0?40:36));
                if(amount<.98)throw new IOException(
                    "Sun Peak stationary obstacle/bird survives reconstruction: "+
                    point[0]+","+point[1]+", alpha="+amount);
            }
        }
        // Sample local painted colours, including the source Sun Peak
        // sunset, instead of introducing unrelated procedural sky bands.
        // All six restored worlds now use their OWN original sky paint.
        // Moon Glow was the only remaining world filled with generic
        // procedural colours, leaving a flat blue rectangle beside the moon.
        int[] matchedSky=boundaryMatchedSky(
            original,world,sourceMatchedSky(original,world));
        if(matchedSky!=null)for(int y=1;y<h;y++){
            // Reject a hard horizontal colour seam in the reconstructed sky.
            // Original floating islands/clouds remain untouched outside masks.
            int from=matchedSky[(y-1)*w+w/2],to=matchedSky[y*w+w/2];
            for(int shift:new int[]{0,8,16})
                if(Math.abs(((from>>>shift)&255)-((to>>>shift)&255))>16)
                    throw new IOException("Hard world-"+world+
                        " replacement-sky band at row "+y);
        }
        CoinCircle[] staticCoins=photographedCoins(world);
        int[][] coinSky=new int[staticCoins.length][];
        for(int i=0;i<staticCoins.length;i++)
            coinSky[i]=coinSkyColours(original,staticCoins[i]);
        BufferedImage output=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        int preserved=0, sampled=0;
        int originalIslandPixels=0, islandSamples=0;
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            double amount=0;
            // An 18px feather removes visible rectangular mask edges without
            // bleeding 90+ pixels into valuable original illustrated scenery.
            // Worlds 3-6 retain their proven legacy mask/feather geometry here.
            for(int mask=0;mask<MASKS[world-1].length;mask++){
                Region shape=MASKS[world-1][mask];
                // Header replacement spans the whole width and needs a
                // broad fade to avoid a sharp horizontal cut across clouds.
                // Keep v0.3.13 masks byte-for-byte consistent for worlds 1-2.
                // Only the newly reconstructed worlds 3-4 use 32px fades.
                double feather=world<=2?(mask==0?72:27):
                               world<=4?(mask==0?72:32):
                               world==6?(mask==0?40:36):92;
                amount=Math.max(amount,shape.alpha(x,y,feather));
            }
            for(Area a:COINS[world-1])
                amount=Math.max(amount,a.alpha(x,y,68));
            double coinAlpha=0;
            int coveredCoin=-1;
            for(int i=0;i<staticCoins.length;i++){
                double a=staticCoins[i].alpha(x,y,17);
                if(a>coinAlpha){ coinAlpha=a;coveredCoin=i; }
                amount=Math.max(amount,a);
            }
            // The retention gate is deliberately in the gameplay strip,
            // excluding the source screenshot's static top HUD and bottom UI.
            if(world<=4 && x%4==0 && y%4==0 && y>=210 && y<1100){
                sampled++;
                if(amount<.08) preserved++;
            }
            if(world==2 && x>=40 && x<280 && y>=750 && y<825
                    && x%4==0 && y%4==0){
                islandSamples++;
                if(amount<.02)originalIslandPixels++;
            }
            int from=original.getRGB(x,y);
            int to=matchedSky[y*w+x];
            if(coveredCoin>=0 && coinAlpha>0)
                to=coinBackdrop(coinSky[coveredCoin],staticCoins[coveredCoin],y);
            int r=mix((from>>>16)&255,(to>>>16)&255,amount),
                g=mix((from>>>8)&255,(to>>>8)&255,amount),
                b=mix(from&255,to&255,amount);
            output.setRGB(x,y,(r<<16)|(g<<8)|b);
        }
        if(world==5) {
            // The former enormous Moon Glow rectangle destroyed whole
            // floating islands/clouds around the baked bird. Check that a
            // substantial portion of the middle source painting survives.
            int total=0,intact=0;
            for(int y=540;y<692;y+=4)for(int x=30;x<686;x+=4) {
                total++;
                double alpha=0;
                for(Region region:MASKS[4])
                    alpha=Math.max(alpha,region.alpha(x,y,36));
                if(alpha<.01)intact++;
            }
            double fraction=intact/(double)Math.max(1,total);
            System.out.printf(
                "SkyPeep Moon Glow: %.1f%% authentic midground retained%n",
                fraction*100);
            if(fraction<.37)
                throw new IOException(
                    "Moon Glow original midground was erased again: retained="+fraction);
            // Preserve the real illustrated crescent and source scenery,
            // independently of the restored sky-field implementation.
            // The photographed Moon Glow coin group occupies x=339..694,
            // y=560..751 and has a separate intentional removal mask.
            // Sample genuine moon/island pixels safely OUTSIDE both masks.
            for(int[] point:new int[][]{{115,345},{650,400},{650,850}}) {
                int x=point[0],y=point[1];
                if((output.getRGB(x,y)&0x00ffffff)!=
                   (original.getRGB(x,y)&0x00ffffff))
                    throw new IOException(
                        "Original Moon Glow art was modified at "+x+","+y);
            }
        }
        if(world==2){
            // Pixel-integrity guard for the entire illustrated island/bridge
            // landscape, not only the five individual landmarks. The previous
            // overwide plume oval painted nearly 100% of this ROI blue.
            double intact=originalIslandPixels/(double)Math.max(1,islandSamples);
            System.out.printf("SkyPeep Cloud Pass island ROI: %.1f%% source pixels untouched%n",
                100*intact);
            if(intact<.88)throw new IOException(
                "Cloud Pass island/bridge artwork damaged: kept="+intact);
            // Reference-fidelity regression: this photographed floating island
            // and its waterfall are not game objects, so source pixels MUST
            // remain unchanged before JPEG encoding. Earlier overly broad
            // foliage-removal masks painted these positions flat blue.
            final int[][] protectedPixels={
                {75,780},{100,780},{160,783},{100,800},{245,810},
                // Original Cloud Pass cloud crown, NOT fixed-pillar plume.
                {280,750},{300,750}
            };
            for(int[] point:protectedPixels){
                int x=point[0],y=point[1];
                if((output.getRGB(x,y)&0x00ffffff)
                    !=(original.getRGB(x,y)&0x00ffffff)){
                    throw new IOException(
                        "Cloud Pass illustrated island/waterfall erased at "
                        +x+","+y+": source="+Integer.toHexString(original.getRGB(x,y))
                        +", actual="+Integer.toHexString(output.getRGB(x,y)));
                }
            }
            System.out.println(
                "SkyPeep world 2: original island, waterfall and cloud ROI source pixels intact");
        }
        // Verify that removing the baked-in bird/obstacles does not destroy
        // real islands, the moon, waterfalls or distant architecture. These
        // coordinates belong to the ORIGINAL 706x1254 source art; checks run
        // on the RGB framebuffer BEFORE JPEG compression.
        if(world==3 || world==4){
            int[][] scenery=world==3?
                new int[][]{{210,270},{260,390},{240,450},
                            {95,380},{670,650},{160,885},{675,960}}:
                new int[][]{{95,370},{150,290},{650,300},
                            {260,420},{100,930},{620,800}};
            for(int[] point:scenery){
                int x=point[0],y=point[1];
                if((output.getRGB(x,y)&0x00ffffff)
                    !=(original.getRGB(x,y)&0x00ffffff))
                    throw new IOException("Original illustrated scenery erased in world "
                        +world+" at "+x+","+y);
            }
            System.out.println("SkyPeep world "+world+
                ": protected original landscape source pixels intact ("+
                scenery.length+" checks)");
        }
        if(world<=4){
            double fraction=preserved/(double)Math.max(1,sampled);
            System.out.printf("SkyPeep world %d: %.1f%% original midground retained%n",
                world,fraction*100.0);
            double threshold=world<=2?.49:.48;
            if(fraction<threshold)throw new IOException(
                "Illustrated scene lost too much original detail in world "+world+
                ": retained="+fraction+" required="+threshold);
        }
        ImageWriter writer=ImageIO.getImageWritersByFormatName("jpeg").next();
        try(OutputStream o=Files.newOutputStream(file);
            ImageOutputStream stream=ImageIO.createImageOutputStream(o)) {
            writer.setOutput(stream);
            ImageWriteParam options=writer.getDefaultWriteParam();
            options.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            options.setCompressionQuality(.91f);
            writer.write(null,new IIOImage(output,null,null),options);
        } finally {writer.dispose();}
        if(Files.size(file)<20000)throw new IOException("Invalid clean art size: "+file);
    }
    /** Clears cropped screenshot header fragments without changing source artwork. */
    private static void removePillarHeader(Path assets,int world) throws IOException {
        Path file=assets.resolve("pillar_"+world+"_top.png");
        BufferedImage original=ImageIO.read(file.toFile());
        if(original==null)throw new IOException("Missing illustrated pillar: "+file);
        BufferedImage image=new BufferedImage(original.getWidth(),original.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        g.drawImage(original,0,0,null);
        g.dispose();
        // Per-image crop thresholds checked against the supplied six top sprites.
        // Preserve the original cap, leaves, clouds, crystals and gold emblems.
        int[][] trim={{0,0},{166,35},{232,20},{199,26},{0,0},{298,16},{0,0}};
        int width=Math.min(image.getWidth(),trim[world][0]);
        int height=Math.min(image.getHeight(),trim[world][1]);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++)
            image.setRGB(x,y,0);
        if(!ImageIO.write(image,"png",file.toFile()))
            throw new IOException("PNG writer unavailable: "+file);
        if(width>0 && ((ImageIO.read(file.toFile()).getRGB(0,0)>>>24)!=0))
            throw new IOException("Header fragment remains in "+file);
        if(Files.size(file)<5000)
            throw new IOException("Truncated illustrated pillar: "+file);
    }


    /**
     * Remove the hard-coded '63' and '4' from the six original wallet plates.
     * Interpolate within carefully measured navy-field rectangles, preserving
     * every pixel outside the ROI: gold coin, magenta gem, borders and corners.
     * Source artwork stays immutable in the SHA-256 validated ZIP.
     */
    private static void cleanHudCounters(Path assets,int world) throws IOException {
        final int[][] left={
            {132,30,212,91},{113,30,206,105},{119,29,209,105},
            {103,30,193,92},{113,29,209,95},{127,30,209,91}
        };
        final int[][] right={
            {108,30,160,93},{106,30,165,102},{106,30,171,102},
            {110,30,162,95},{112,30,181,94},{111,30,161,95}
        };
        for(int side=0;side<2;side++){
            String name=side==0?"left":"right";
            int[] box=(side==0?left:right)[world-1];
            Path path=assets.resolve("hud_"+name+"_"+world+".png");
            BufferedImage input=ImageIO.read(path.toFile());
            int width=side==0?269:191, height=side==0?113:115;
            if(input==null || input.getWidth()!=width || input.getHeight()!=height)
                throw new IOException("Unexpected HUD crop size for "+path);
            BufferedImage result=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<height;y++)for(int x=0;x<width;x++) {
                int original=input.getRGB(x,y);
                int color=original;
                if(x>=box[0] && x<box[2] && y>=box[1] && y<box[3]) {
                    double blend=Math.min(1.,Math.min(
                        Math.min((x-box[0])/5.,(box[2]-1-x)/5.),
                        Math.min((y-box[1])/4.,(box[3]-1-y)/4.)));
                    if(blend>0) {
                        double fraction=(x-box[0]+1.)/(box[2]-box[0]+1.);
                        int a=input.getRGB(box[0]-1,y),b=input.getRGB(box[2],y);
                        int r=mix((a>>>16)&255,(b>>>16)&255,fraction);
                        int g=mix((a>>>8)&255,(b>>>8)&255,fraction);
                        int blue=mix(a&255,b&255,fraction);
                        // Worlds 2/3/5 have ornate gem borders intersecting
                        // horizontal samples. A stable navy-field source
                        // avoids cyan/magenta bands across cleaned digits.
                        if(side==1 && (world==2 || world==3 || world==5)){
                            int sampleX=world==2?160:(world==3?165:174);
                            int sampleY=Math.min(87,Math.max(36,y));
                            int navy=input.getRGB(sampleX,sampleY);
                            r=(navy>>>16)&255;
                            g=(navy>>>8)&255;
                            blue=navy&255;
                        }
                        int outR=mix((original>>>16)&255,r,blend);
                        int outG=mix((original>>>8)&255,g,blend);
                        int outB=mix(original&255,blue,blend);
                        color=(original&0xFF000000)|(outR<<16)|(outG<<8)|outB;
                    }
                }
                result.setRGB(x,y,color);
            }
            // Pixel-by-pixel identity outside the intentional text region.
            for(int y=0;y<height;y++)for(int x=0;x<width;x++)
                if((x<box[0]||x>=box[2]||y<box[1]||y>=box[3])
                    && input.getRGB(x,y)!=result.getRGB(x,y))
                    throw new IOException("HUD icon or border modified: "+path);
            if(!ImageIO.write(result,"png",path.toFile()))
                throw new IOException("PNG encoder missing: "+path);
            if(Files.size(path)<10000)
                throw new IOException("HUD output suspiciously small: "+path);
        }
    }


    /** Source blue pixels sampled around the numerals on the real winged badge. */
    private record BadgeSkySample(int x,int y,int red,int green,int blue) { }

    /** Match the ORIGINAL score-shield paint for each illustrated world.
     * Some sky badges are PALE (Cloud Drift) or NAVY (Wind Gate, Sun Peak),
     * not bright cyan. A one-palette rule destroyed the actual world design.
     */
    private static boolean originalBadgeBackground(int rgb,int world) {
        int r=(rgb>>>16)&255,g=(rgb>>>8)&255,b=rgb&255;
        return switch(world) {
            case 1 -> r<125 && g>90 && g<230 && b>155 && b>g+8;
            case 2 -> r>180 && r<246 && g>215 && b>236 && b-r>8;
            case 3 -> r<154 && g>155 && b>215 && b>g+9;
            case 4 -> r>=8 && r<70 && g>28 && g<117 &&
                      b>60 && b<160 && b>g+20;
            case 5 -> r<90 && g>112 && g<235 && b>175 && b>g+12;
            case 6 -> r<185 && g>105 && b>190 && b>=g+5 && b>r+50;
            default -> false;
        };
    }

    /**
     * Round mask with a 9px feather, never a new rectangular HUD plate.
     * Its small border overlaps remove the source score's dark antialiasing,
     * but the surrounding wing/cloud outlines remain original source pixels.
     */
    private static double badgeMask(int x,int y,
                                    int x1,int y1,int x2,int y2) {
        double radius=31.0;
        double cx=(x1+x2)/2.0,cy=(y1+y2)/2.0;
        double qx=Math.abs(x-cx)-(x2-x1)/2.0+radius;
        double qy=Math.abs(y-cy)-(y2-y1)/2.0+radius;
        double d=Math.hypot(Math.max(0,qx),Math.max(0,qy))
                 +Math.min(Math.max(qx,qy),0)-radius;
        return clamp(.5-d/14.0);
    }

    /**
     * Replace only the numeral in the ORIGINAL illustrated badge, borrowing colour
     * from its unoccluded blue brush strokes. No new illustration, cream
     * rectangle, or third-party inpainting library is involved.
     */
    private static void cleanScoreHeader(Path assets,int world) throws IOException {
        Path file=assets.resolve("hud_score_"+world+".png");
        BufferedImage original=ImageIO.read(file.toFile());
        if(original==null || original.getWidth()!=367 || original.getHeight()!=210)
            throw new IOException("Unexpected illustrated score header: "+file);
        final int width=367,height=210;
        BufferedImage cleaned=new BufferedImage(width,height,
                                                 BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++)
            cleaned.setRGB(x,y,original.getRGB(x,y));

        // For the six supplied source PNGs, the illustrated cloud/wings and
        // title ribbon MUST remain untouched. Only a feathered numeral
        // interior, bounded by that world's old score-replacement envelope,
        // may be reconstituted.
        final int[][] bounds={
            {91,27,276,150},{106,28,264,143},{106,31,267,135},
            {108,29,261,122},{120,27,257,116},{105,24,272,146}
        };
        int[] b=bounds[world-1];
        // Each score illustration has its OWN inner shield silhouette.
        // These feathered insets follow the underlying winged artwork
        // rather than painting a single rectangular patch over every world.
        final int[][] scoreInsets={
            {112,40,271,144},{121,42,251,141},{119,34,255,129},
            {128,37,246,124},{125,27,251,120},{118,35,254,131}
        };
        int[] v=scoreInsets[world-1];
        int mx1=v[0],my1=v[1],mx2=v[2],my2=v[3];

        java.util.ArrayList<BadgeSkySample> samples=new java.util.ArrayList<>();
        for(int y=18;y<139;y+=3)for(int x=76;x<300;x+=3) {
            int rgb=original.getRGB(x,y);
            if(originalBadgeBackground(rgb,world))
                samples.add(new BadgeSkySample(x,y,
                    (rgb>>>16)&255,(rgb>>>8)&255,rgb&255));
        }
        if(samples.size()<24)throw new IOException(
            "Insufficient original cyan badge pixels in world "+world+
            ": "+samples.size());

        // Some original world badges have sparse cyan around the numerals;
        // look across the whole illustrated shield, not only a 66px core.
        final int distance=130,sigma=22;
        double[] weight=new double[distance*distance*2+1];
        for(int d2=0;d2<weight.length;d2++)
            weight[d2]=Math.exp(-d2/(2.0*sigma*sigma));

        for(int y=Math.max(0,my1-12);y<Math.min(height,my2+13);y++)
            for(int x=Math.max(0,mx1-12);x<Math.min(width,mx2+13);x++) {
                // The old replacement allowed changes only in this rectangle.
                // Protect source artwork and title outside it bit-for-bit.
                if(x<b[0]-3 || x>b[2]+3 ||
                   y<b[1]-3 || y>b[3]+3)continue;
                double alpha=badgeMask(x,y,mx1,my1,mx2,my2);
                if(alpha<=0)continue;
                double rs=0,gs=0,bs=0,total=0;
                for(BadgeSkySample sample:samples) {
                    int dx=x-sample.x,dy=y-sample.y;
                    if(Math.abs(dx)>distance||Math.abs(dy)>distance)continue;
                    double w=weight[dx*dx+dy*dy];
                    rs+=sample.red*w;
                    gs+=sample.green*w;
                    bs+=sample.blue*w;
                    total+=w;
                }
                if(total<.00001)continue;
                int base=original.getRGB(x,y);
                int red=mix((base>>>16)&255,(int)Math.round(rs/total),alpha);
                int green=mix((base>>>8)&255,(int)Math.round(gs/total),alpha);
                int blue=mix(base&255,(int)Math.round(bs/total),alpha);
                cleaned.setRGB(x,y,(base&0xff000000)|
                                  (red<<16)|(green<<8)|blue);
            }

        // An opaque beige/white rectangle is not part of the original
        // reference. Confirm the rebuilt centre is still a blue sky badge.
        int middle=cleaned.getRGB((mx1+mx2)/2,(my1+my2)/2);
        if(!originalBadgeBackground(middle,world))throw new IOException(
            "Score badge no longer matches its original sky colour in world "+world);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++)
            if((x<b[0]-3 || x>b[2]+3 || y<b[1]-3 || y>b[3]+3) &&
               original.getRGB(x,y)!=cleaned.getRGB(x,y))
                throw new IOException("Protected wing or ribbon changed: "+file);
        if(!ImageIO.write(cleaned,"png",file.toFile()) ||
           Files.size(file)<18000)
            throw new IOException("Invalid original-art score badge: "+file);
        System.out.println("PASS: source-matched illustrated badge world "+world+
                           " ("+samples.size()+" original sky samples)");
    }

    public static void main(String[] args) throws Exception {
        if(args.length==1 && args[0].equals("--self-test")) {
            verifySkyReconstruction();
            return;
        }
        if(args.length!=2) throw new IllegalArgumentException(
            "Usage: java -Djava.awt.headless=true tools/RebuildWorlds.java <assets-directory> <sun-peak-original-reference.jpg>");
        for(int level=1;level<=6;level++){
            Path path=Path.of(args[0],"bg_"+level+".jpg");
            rebuild(path,level,Path.of(args[1]));
            removePillarHeader(Path.of(args[0]),level);
            cleanHudCounters(Path.of(args[0]),level);
            cleanScoreHeader(Path.of(args[0]),level);
            System.out.println("CLEAN WORLD "+level+" "+Files.size(path)+" bytes; wallets and score face restored");
        }
    }
}
