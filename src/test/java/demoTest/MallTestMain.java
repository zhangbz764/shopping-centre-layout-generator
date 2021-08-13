package demoTest;

import basicGeometry.ZPoint;

import java.util.ArrayList;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/6/17
 * @time 11:08
 */
public class MallTestMain {
    public static void main(String[] args) {
//        ZPoint dd = new ZPoint(0, 0, 1);
//        ArrayList list = new ArrayList();
//        list.add("f");
//        list.add(1);
//        list.add(new ZPoint(0, 0, 1));
//        System.out.println(list);
//
//        String s = (String) list.get(0);
//        int i = (int) list.get(1);
//        ZPoint p = (ZPoint) list.get(2);
//
//        System.out.println(p);
        TestJson t = new TestJson();
//        t.runSave();
        t.runLoad();
    }
}
