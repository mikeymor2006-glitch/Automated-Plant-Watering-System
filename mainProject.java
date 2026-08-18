import edu.princeton.cs.introcs.StdDraw;
import org.firmata4j.*;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import java.io.IOException;
import java.util.ArrayList;

public class mainProject {
    private static IODevice myGroveBoard = new FirmataDevice("COM5");
    private static SSD1306 theOledObject;
    private static Pin Sensor;
    private static Pin pump;
    private static double saturatedValue = 1.8; // soil is saturated with water at 2.6V
    private static double reallyDryValue = 2.5;
    private static  ArrayList<Double> sensorValues = new ArrayList<>();
    private static ArrayList<Double> time = new ArrayList<>();
    private static double startTime;

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            myGroveBoard.start(); //starts the board
            Thread.sleep(3000);
            myGroveBoard.ensureInitializationIsDone(); // ensure that grove board is initialized
            System.out.println("Board initialized successfully.");

            I2CDevice i2cObject = myGroveBoard.getI2CDevice((byte) 0x3C); // Use 0x3C for the Grove OLED
            theOledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64); // 128x64 OLED SSD1515
            theOledObject.init(); // initialize Oled object
            System.out.println("OLED is initialized.");

            Sensor = myGroveBoard.getPin(15); // get voltage value of soil-moisture
            Sensor.setMode(Pin.Mode.ANALOG);
            pump = myGroveBoard.getPin(7); // get output value reading for pump
            pump.setMode(Pin.Mode.OUTPUT);

            setupGraphScales();
            drawAxesandLabelGraph();
            startTime = System.currentTimeMillis();
            stateMachine(1000,startTime);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            cleanup();
            myGroveBoard.stop();
        }
    }
    private static void cleanup() {
        if (myGroveBoard != null) {
            try {
                myGroveBoard.stop();
                System.out.println("Board stopped.");
            } catch (IOException e) {
                System.err.println("Stop failed: " + e.getMessage());
            }
        }
    }
    public static void setupGraphScales() {
        StdDraw.setCanvasSize(1540, 800);
        StdDraw.setXscale(0, 30);
        StdDraw.setYscale(0, 3.5);
    }
    public static void drawAxesandLabelGraph(){
        StdDraw.setPenRadius(0.006);
        StdDraw.setPenRadius(0.007);
        StdDraw.line(0,0,30,0); //x-axis
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.line(0,0,0,3.4); // y-axis
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(27, -0.1, "Time[min]");
        StdDraw.text(-0.8, 2.5, "[V]");
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.text(15, 3.5, "Sensor Voltage vs Time");
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.007);

        StdDraw.line(-0.2,3.0,0.2,3.0);
        StdDraw.line(-0.2,1.5,0.2,1.5);
        StdDraw.text(-0.95,3.0,"3.0");
        StdDraw.text(-1.0,1.5,"1.5");
        StdDraw.setPenRadius(0.004);

        StdDraw.line(30,-0.025,30,0.025);
        StdDraw.line(15,-0.025,15,0.025);
        StdDraw.text(30,-0.10,"30");
        StdDraw.text(15,-0.10,"15");
        StdDraw.setPenRadius(0.004);
    }
    public static void stateMachine(int max_Samples, double startTime)
            throws IOException, InterruptedException {
        for (int i = 0; i <= max_Samples; i++) {
            double VoltageValue = (Sensor.getValue() * 5.0) / 1023.0; // convert raw analog values to volts
            double timeValue = ((double) System.currentTimeMillis() - startTime) / 60000.0; //convert time in sec to minutes
            sensorValues.add(VoltageValue);
            time.add(timeValue);
            // voltage greater than or equal to 3.5 V, turn on pump
            if (VoltageValue >= reallyDryValue) {
                theOledObject.getCanvas().drawString(0, 0, "Pump is on");
                theOledObject.getCanvas().drawString(0, 15, "Soil is dry");
                System.out.println("Soil moisture: " + VoltageValue);
                pump.setValue(1); //turn on sensor
            }
            if (VoltageValue < reallyDryValue && VoltageValue >= saturatedValue){ //if voltage greater 2.5 V or less than or to 3.5 V turn pump on
                theOledObject.getCanvas().drawString(0, 0, "Pump is on");
                theOledObject.getCanvas().drawString(0, 15, "Soil is wet");
                System.out.println("Soil moisture: " + VoltageValue);
                pump.setValue(1); //turn on pump
            }
            if (VoltageValue < saturatedValue){ //if voltage less than or equal to 2.5 V, turn off pump
                theOledObject.getCanvas().drawString(0, 0, "Pump is off");
                theOledObject.getCanvas().drawString(0, 15, "Soil is saturated with water");
                System.out.println("Soil moisture: " + VoltageValue);
                pump.setValue(0); //turn off pump
            }
            theOledObject.display(); //uodate display
            StdDraw.point(time.get(i), sensorValues.get(i));
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius(0.008);
            Thread.sleep(1000); // prevents the pump to bring up anymore water in this duration of seconds
        }

    }
}




