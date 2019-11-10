import TestLink.StepResult;
import TestLink.TestLinkMain;
import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.Attachment;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import net.serenitybdd.jbehave.SerenityStory;
import net.thucydides.core.model.*;
import net.thucydides.core.steps.StepEventBus;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class imgArranger extends SerenityStory{
    protected TestOutcome testOutcome;
    protected String errorMessage = null;
    protected Boolean isSuccess = true;
    protected List<String> exampleValues = new ArrayList<>();
    protected List<StepResult> stepResults =  new ArrayList<StepResult>();

    String TESTLINK_URL = "http://testlink.sepulsa.id/lib/api/xmlrpc/v1/xmlrpc.php";
    String TESTLINK_KEY = "ded5b694d9d0058a9f646b6cb12af480";

    String _PROJECTNAME = "CM - ERP";
    Integer _SUITEID;
    Integer _PROJECTID = 927;
    Integer _VERSION = 1;
    String _BUILDNAME = "test";
    String _PLANNAME = "Sprint Goals 22 Okt 2019 - 5 Nov 2019";
    String _USERNAME = "anjas";
    String _TCSUMMARY = "Tested by Anjas";

    //Image Compressor By Anjas//
    //write '0' to set default value (default value = 80 KB, max image size)
    //Min of maxImgSize is 10, if u set under 10, it will be automatically set to 10
    int maxImgSize = 40; //max image size each step in KiloBytes
    //Image uploader by Anjas
    int execID = 37191; //Change into your execution ID

    @AfterScenario(uponType = ScenarioType.NORMAL)
    public void TestLinkIntegration() throws IOException {
        this.testOutcome = GetTestOutcome();

        String TestCaseName =  this.testOutcome.getName();

        //  Suite ID from scenario name with pattern '1234567'
        this._SUITEID = ParseSuiteID(TestCaseName);

        GetTestResult();
        GetTestErrorMessage();

        TestLinkMain testLinkMain = new TestLinkMain(TESTLINK_URL, TESTLINK_KEY);
        testLinkMain.Init(_PROJECTNAME, _PROJECTID, _VERSION, _BUILDNAME, _PLANNAME, _USERNAME);

        if(stepResults.size() > 0) {
            testLinkMain.Run(TestCaseName, this.isSuccess, this.errorMessage, this.stepResults, this._SUITEID);
        }
    }

    String TestCaseName = null;
    static String TestCaseNameOld = null;
    @AfterScenario(uponType = ScenarioType.EXAMPLE)
    public void TestLinkIntegrationforExample() {
        this.testOutcome = GetTestOutcome();
        TestCaseName =  this.testOutcome.getName();
        //  Suite ID from scenario name with pattern '1234567'
        this._SUITEID = ParseSuiteID(TestCaseName);
        if(!TestCaseName.equals(TestCaseNameOld)){
            GetTestResultWithExample();
            GetTestErrorMessage();
            TestLinkMain testLinkMain = new TestLinkMain(TESTLINK_URL, TESTLINK_KEY);
            testLinkMain.Init(_PROJECTNAME, _PROJECTID, _VERSION, _BUILDNAME, _PLANNAME, _USERNAME);
            if(stepResults.size() > 0) {
                testLinkMain.Run(TestCaseName, this.isSuccess, this.errorMessage, this.stepResults, this._SUITEID);
            }
        }
        TestCaseNameOld = this.testOutcome.getName();;
    }

    private TestOutcome GetTestOutcome() {
        List<TestOutcome> testOutcomeList= StepEventBus.getEventBus().getBaseStepListener().getTestOutcomes();

        TestOutcome testOutcome = testOutcomeList.get(testOutcomeList.size()-1);

        return testOutcome;
    }

    private void GetTestErrorMessage() {
        if(testOutcome.isFailure() || testOutcome.isError()) {
            errorMessage = testOutcome.getErrorMessage();
            isSuccess = false;
        }
    }

    private void GetTestResultWithExample() {
        Integer count_given = 0;
        List<TestStep> testStepList = this.testOutcome.getTestSteps();
        for(TestStep testStep : testStepList) {
            int countFailure=0;
            Boolean exampleStories = testStep.getDescription().toString().matches("(.*)Example #\\d+(.*)");

            // GivenStories
            if(testStep.getChildren().size() > 0 && !exampleStories && stepResults.size() == count_given) {
                stepResults.clear();
                StepResult stepResult = new StepResult();
                stepResult.name = testStep.getDescription();
                stepResult.status = testStep.isFailure() || testStep.isError() ? "Failed" : "Success";
                stepResults.add(stepResult);
                count_given++;
            }

            if(testStep.getChildren().size() > 0 && exampleStories) {
                // Jbehave scenario with examples
                DataTable dataTable = this.testOutcome.getDataTable();
                List<String> exampleFields = dataTable.getHeaders();
                List<DataTableRow> exampleValuesRow = dataTable.getRows();

                if (this.exampleValues.size() == 0) {
                    for (DataTableRow dataTableRow : exampleValuesRow) {
                        List<String> values_temp = dataTableRow.getStringValues();
                        for (String value : values_temp) {
                            this.exampleValues.add(value.toString());
                        }
                    }
                }

                if(_TCSUMMARY == null) {
                    _TCSUMMARY += "fields = " + String.join(",", exampleFields) + "\n";
                    _TCSUMMARY += "values = " + String.join(",", this.exampleValues) + "\n";
                }

                if(stepResults.size() == count_given) {
                    List<TestStep> childrenSteps = testStep.getChildren();

                    int count = 0;
                    String storyName = testOutcome.getFeatureTag().get().getName();
                    //storyName = storyName.substring(0,storyName.lastIndexOf("/"));
                    try{
                        this.TestCaseName =  this.testOutcome.getName();
                        TestCaseName = TestCaseName.substring(TestCaseName.lastIndexOf("-")+2,TestCaseName.length());
                    }catch (Exception e){
                        TestCaseName =  this.testOutcome.getName();
                    }

                    for (TestStep testStep1 : childrenSteps) {
                        System.out.println("Selesai Step");
                        StepResult stepResult = new StepResult();
                        stepResult.name = testStep1.getDescription();
                        stepResult.status = testStep1.isFailure() || testStep.isError() ? "Failed" : "Success";
                        stepResults.add(stepResult);

                        //DI SINI PERSTEP
                        if(stepResult.status.equals("Failed")) countFailure++;
                        if(countFailure<=1){
                            count++;
                            try{
                                if(testStep.getChildren().get(count-1).getScreenshots().get(0).getScreenshot().exists()){
                                    String direktoriSS = testStep.getChildren().get(count-1).getScreenshots().get(0).getScreenshot().getPath();
                                    compressImage(direktoriSS,storyName,TestCaseName,"Step "+count,count);
                                }
                            }catch (Exception e){
                                System.out.println("Gagal comprees karena "+e.getMessage());
                            }
                        }
                        //}
//
//                    count++;
//                    if(testOutcome.getTestSteps().size() == 1) {
//                        for (count = (count - 1); count > 0; count--) {
//                            String dirFile = "./ScreenShots/"+storyName+"/"+TestCaseName+"/"+"Step "+count+".jpg";
//                            //uploadImage(execID, dirFile, "Step " + count, TestCaseName);
//                        }
                    }
                }
            }
        }
    }

    private void GetTestResult() throws IOException {
        _TCSUMMARY = this.testOutcome.getName();
        List<TestStep> testStepList = this.testOutcome.getTestSteps();

        int count = 0, countFailure=0;
        String direktoriSS = null;
        Boolean flagSuccess = false;
        String storyName = testOutcome.getFeatureTag().get().getName();
        //storyName = storyName.substring(0,storyName.lastIndexOf("/"));
        try {
            TestCaseName = this.testOutcome.getName();
            TestCaseName = TestCaseName.substring(TestCaseName.lastIndexOf("-") + 2, TestCaseName.length());
        } catch (Exception e) {
            TestCaseName = this.testOutcome.getName();
        }

        for (TestStep testStep : testStepList) {
            flagSuccess = false;
            Pattern pattern = Pattern.compile("^(Given|When|Then)");
            Matcher matcher = pattern.matcher(testStep.getDescription());
            Boolean storyStep = matcher.find();

            if (testStep.getChildren().size() > 0 && !storyStep) {
                stepResults.clear();
                StepResult stepResult = new StepResult();
                stepResult.name = testStep.getDescription();
                stepResult.status = testStep.isFailure() || testStep.isError() ? "Failed" : "Success";
                flagSuccess = testStep.isFailure() || testStep.isError() ? false : true;
                stepResults.add(stepResult);

            }

            if (testStep.getChildren().size() == 0) {
                StepResult stepResult = new StepResult();
                stepResult.name = testStep.getDescription();
                stepResult.status = testStep.isFailure() || testStep.isError() ? "Failed" : "Success";
                flagSuccess = testStep.isFailure() || testStep.isError() ? false : true;
                stepResults.add(stepResult);
            }

            if (testStep.getChildren().size() > 0 && storyStep) {
                List<TestStep> childrenSteps = testStep.getChildren();
                for (TestStep testStep1 : childrenSteps) {
                    StepResult stepResult = new StepResult();
                    stepResult.name = testStep1.getDescription();
                    stepResult.status = testStep1.isFailure() || testStep.isError() ? "Failed" : "Success";
                    flagSuccess = testStep.isFailure() || testStep.isError() ? false : true;
                    stepResults.add(stepResult);
                }
            }

            if(!flagSuccess) countFailure++;
            if (countFailure<=1) {
                count++;
                try {
                    if(testStep.getScreenshots().get(0).getScreenshot().exists()){
                        direktoriSS = testStep.getScreenshots().get(0).getScreenshot().getPath();
                        compressImage(direktoriSS,storyName,TestCaseName,"Step " +count,count);
                    }
                } catch (Exception e) {
                    System.out.println("Gagal comprees karena " + e.getMessage());
                }
            }
        }

//        //Perlu flag success karena di kasus tanpa example, count selalu bertambah walaupun failed atau succes, jadi perlu penanda sukses
//        count++;
//        for (count=(count-1);count>0;count--) {
//            String dirFile = "./ScreenShots/"+storyName+"/"+TestCaseName+"/"+"Step "+count+".jpg";
//            uploadImage(execID, dirFile, " Step " + count, TestCaseName);
//        }
    }

    private Integer ParseSuiteID(String TestCaseName) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(TestCaseName);

        if(matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private void renameFile(String oldName, String newName, String ScenarioName) {
        try {
            File sourceFile = new File(oldName);
            FileUtils.copyFile(sourceFile, new File("./ScreenShots/"+ScenarioName+"/"+newName+".png"));
        }
        catch (Exception e)
        {
            System.out.println("=========== ERROR MEMINDAI SS ==============");
            System.out.println("Name Of Scenario: "+ScenarioName);
            System.out.println("Step ke- "+ScenarioName.substring(6,ScenarioName.length()));
            System.out.println("Alasan kegagalan: "+e.getMessage());
            System.out.println("=========== ERROR MEMINDAI SS ==============");
        }
    }

    private void compressImage(String sourceFile, String storyName, String tcName, String targetDirectory, int cntStep) throws IOException {
        String direktoriFile = "./ScreenShots/"+storyName+"/"+tcName+"/"+targetDirectory+".jpg";
        File dirSS = new File("./ScreenShots");
        File dirBaru = new File("./ScreenShots/"+storyName+"/"+tcName);
        if(!dirSS.exists()){
            dirSS.mkdir();
            while (!dirSS.exists());
        }
        if(dirBaru.exists() && cntStep == 1){
            delete(dirBaru);
            while (dirBaru.exists());
            dirBaru.mkdirs();
            while (!dirBaru.exists());
        }else if(!dirBaru.exists()){
            dirBaru.mkdirs();
            while (!dirBaru.exists());
        }
        BufferedImage bufferedImage;
        bufferedImage = ImageIO.read(new File(sourceFile));
        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
        ImageIO.write(newBufferedImage, "jpg", new File(direktoriFile));
        waterMarkImg(direktoriFile,"./ScreenShots/"+storyName+"/"+tcName+"/"+targetDirectory);

        File gambarCompress = new File(direktoriFile);
        maxImgSize = maxImgSize < 10 ? maxImgSize==0 ? 80:10 : maxImgSize; //80 = Default value
        if(gambarCompress.exists()){
            float ImgSz = (float) gambarCompress.length()/1024,szTemp = 0;
            int ImgSzOld = 0;
            int cntWhile=0;
            while(ImgSz>maxImgSize){
                if((ImgSzOld == (int) ImgSz && ImgSz<(maxImgSize+1)) || cntWhile>=10) break; //1 = tolerance error size
                //szTemp = ImgSz - maxImgSize;
                //0.5182 = constant calculate manually
                //percentage in dec point
                //szTemp = szTemp>=maxImgSize ? (float) 0.5182/2 : (float) ((0.5182 * (maxImgSize - szTemp) / maxImgSize) < (float) 0.5182 / 2 ? (float) 0.5 * 0.5182 : 0.5182f * (maxImgSize - szTemp) / maxImgSize);
                szTemp = cntWhile==0 ? (float) 0.76*maxImgSize/ImgSz : (float) 0.76*szTemp*maxImgSize/ImgSz;
                BufferedImage image = ImageIO.read(new File(direktoriFile));
                File compressedImageFile = new File(direktoriFile);
                OutputStream os =new FileOutputStream(compressedImageFile);

                Iterator<ImageWriter> writers =  ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer = (ImageWriter) writers.next();

                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                writer.setOutput(ios);

                ImageWriteParam param = writer.getDefaultWriteParam();

                if (param.canWriteCompressed() && szTemp>0 && szTemp<1) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(szTemp);
                    writer.write(null, new IIOImage(image, null, null), param);
                }

                os.close();
                ios.close();
                writer.dispose();

                ImgSzOld = (int) ImgSz;
                ImgSz = (float) gambarCompress.length()/1024;
                cntWhile++;
            }
        }
    }

    private void delete(File file) throws IOException{
        if(file.isDirectory()){
            //directory is empty, then delete it
            if(file.list().length==0){

                file.delete();
                System.out.println("Directory is deleted : " + file.getAbsolutePath());

            }else{

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if(file.list().length==0){
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        }else{
            //if file, then delete it
            file.delete();
            //System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

    private void uploadImage(int ExecId, String direktoriFile, String stepKe, String tcName) {
        String url = TESTLINK_URL;
        String devKey = TESTLINK_KEY;
        TestLinkAPI api = null;

        URL testlinkURL = null;

        try {
            testlinkURL = new URL(url);
        } catch (MalformedURLException mue) {
            mue.printStackTrace(System.err);
            Assert.fail(mue.getMessage());
        }

        try {
            api = new TestLinkAPI(testlinkURL, devKey);
        } catch (TestLinkAPIException te) {
            te.printStackTrace(System.err);
            Assert.fail(te.getMessage());
        }

        File attachmentFile = new File(direktoriFile);

        String fileContent = null;

        try {
            byte[] byteArray = FileUtils.readFileToByteArray(attachmentFile);
            fileContent = new String(Base64.encodeBase64(byteArray));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }

        Attachment attachment = api.uploadExecutionAttachment(ExecId, // executionId
                "Test "+stepKe, // Nama gambar di testlink
                _TCSUMMARY, // description
                stepKe+".jpg", // nama file di testlink
                "image/jpeg", // tipe file
                fileContent); // gbr yg diupload dari direktori file

        System.out.println("Attachment uploaded");

        Assert.assertNotNull(attachment);
    }

    private void waterMarkImg(String originFile, String destination){
        File origFile = new File(originFile);
        ImageIcon icon = new ImageIcon(origFile.getPath());
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();
        graphics.drawImage(icon.getImage(), 0, 0, null);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        FontMetrics fm = graphics.getFontMetrics();
        graphics.setColor(Color.BLACK);
        String wmkImg = "Tested By Anjas";
        graphics.drawString(new String(java.util.Base64.getDecoder().decode("wqkgVGVzdGVkIEJ5IEFOSkFT=")), (fm.stringWidth(wmkImg)/10), icon.getIconHeight()-(fm.getHeight() / 2));// + fm.getAscent());
        graphics.dispose();

        File newFile = new File(destination+".jpg");
        try {
            ImageIO.write(bufferedImage, "jpg", newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(newFile.getPath() + " created successfully!");
    }
}