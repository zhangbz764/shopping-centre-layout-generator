package main.demoTests;

import geometry.ZPoint;
import math.ZGeoMath;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/20
 * @time 14:22
 * @description 测试极角排序
 */
public class TestPolarAngle extends PApplet {
    public void settings() {
        size(1000, 1000, P3D);
    }

    ZPoint oo = new ZPoint(700, 200);
    ZPoint aa = new ZPoint(800, 100);
    ZPoint bb = new ZPoint(500, 500);

    ZPoint[] pts = new ZPoint[6];
    List<ZPoint> vcs;
    ZPoint o = new ZPoint(500, 500);
    int[] sort = new int[pts.length];

    ZPoint bisector;

    public void randomP() {
        vcs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ZPoint pt = new ZPoint(random(200, 800), random(200, 800));
            pts[i] = pt;
            vcs.add(pt.sub(o));
        }
        sort = ZGeoMath.sortPolarAngleIndices(vcs);

    }


    public void setup() {
        println(Math.atan2(0, 20));
        randomP();
    }

    public void mouseClicked() {
        randomP();
    }

    public void draw() {
        background(255);
        ellipse((float) pts[0].x(), (float) pts[0].y(), 5, 5);

        for (int i = 0; i < sort.length; i++) {
            strokeWeight((i + 1) * 2);
            line((float) o.x(), (float) o.y(), (float) pts[sort[i]].x(), (float) pts[sort[i]].y());
            text(i, (float) pts[sort[i]].x(), (float) pts[sort[i]].y());
        }

        bb.set(mouseX, mouseY);
        line((float) oo.x(), (float) oo.y(), (float) aa.x(), (float) aa.y());
        line((float) oo.x(), (float) oo.y(), (float) bb.x(), (float) bb.y());

        ZPoint center = new ZPoint((oo.x() + bb.x()) * 0.5, (oo.y() + bb.y()) * 0.5);
        ZPoint rotate = bb.add(center.sub(bb).unit().rotate2D(Math.PI * 1.25).scaleTo(100));

        line((float) bb.x(), (float) bb.y(), (float) rotate.x(), (float) rotate.y());

        bisector = ZGeoMath.getAngleBisectorOrdered(aa.sub(oo), bb.sub(oo));
        bisector.displayAsVector(this, oo, 50);
    }

}
