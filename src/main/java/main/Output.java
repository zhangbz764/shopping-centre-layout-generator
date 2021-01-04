package main;

/**
 * output all generated model
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/29
 * @time 16:08
 */
public class Output {
    private String outputPath;

    /* ------------- constructor ------------- */

    public Output(String outputPath){
        this.outputPath = outputPath;
    }

    /* ------------- setter ------------- */

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    /* ------------- member function ------------- */

    public void outputJPG() {
        System.out.println("** STARTING FILE OUTPUT **");

        System.out.println("** FILE OUTPUT SUCCESS **");
    }

    public void outputPNG() {

    }

    public void output3DM() {

    }

    public void outputDXF() {

    }

}
