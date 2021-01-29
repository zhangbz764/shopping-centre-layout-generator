package main;

import floors.Floor;
import igeo.IG;

/**
 * output all generated model
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/29
 * @time 16:08
 */
public class Output {

    /* ------------- constructor ------------- */

    public Output(String outputPath) {

    }

    /* ------------- setter ------------- */


    /* ------------- member function ------------- */

    private void saveFloors(Floor floor){

    }

    public void outputJPG() {
        System.out.println("** STARTING FILE OUTPUT **");

        System.out.println("** FILE OUTPUT SUCCESS **");
    }

    public void outputPNG() {

    }

    public void output3DM(String path) {
        IG.save(path);
    }

    public void outputDXF() {

    }

}
