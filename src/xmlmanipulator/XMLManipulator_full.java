/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlmanipulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author suelen
 */
public class XMLManipulator_full {

    /**
     * @param args the command line arguments
     */
    public static HashMap<String, List<String>> smartcarsIDsTable = new HashMap<>();
    boolean useless = mountSmartcarsIDsTable("simpleT", "0.1", "0.01");
    
    public static void main(String[] args) throws FileNotFoundException {
        
        File dir = new File("data");
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        //mods
        //alterar pra o arquivo correto
        //List<String> smartcarsIDs = readFile(".txt");
        
        //end mods
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLManipulator_full.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        HashMap<Double, HashMap<String, String>> table = new HashMap<>();
        HashMap<String, HashMap<String,List<Double>>> vehiclesTable = new HashMap<>(); //id do smart e lista com timesteps de entrada ate saida
        
        if (dBuilder != null) {
            for (File file : files) {
                try {
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    //NodeList nList = doc.getElementsByTagName("step");
                    NodeList nList = doc.getElementsByTagName("data");
                    
                    for (int temp = 0; temp < nList.getLength(); temp++) {
                        
                        Element element = (Element) nList.item(temp);
                        Double time = Double.parseDouble(element.getAttribute("timestep"));
                        
                        
                        //my mods
                        NodeList vehiclesList = element.getElementsByTagName("vehicles");
                        
                        for(int i=0; i < vehiclesList.getLength();i++){
                            Element vehicle = (Element) vehiclesList.item(i);
                            String id = vehicle.getAttribute("id");
                            
                            if (!vehiclesTable.containsKey(id)) {
                                vehiclesTable.put(id, new HashMap<>()); //caso n estivesse na tabela, cria essa entrada guardando o tempo de entrada na simulação
                            }
                            else{
                                if(vehiclesTable.get(id).containsKey(file.getName())){
                                    List<Double> tempList = vehiclesTable.get(id).get(file.getName());
                                    tempList.add(time);
                                    vehiclesTable.get(id).replace(file.getName(), tempList);//adiciona mais um tempo em que o veiculo esteve na simulação
                                }
                                else{
                                    vehiclesTable.get(id).put(file.getName(), Arrays.asList(time));
                                }   
                            }
                        }
                        //end mods
                        
                        //acho q pode sair
                        if (!table.containsKey(time)) {
                            table.put(time, new HashMap<>());
                        }
                        table.get(time).put(file.getName(), element.getAttribute("meanTravelTime"));
                        //end acho q pode sair
                    }
                } catch (SAXException ex) {
                    Logger.getLogger(XMLManipulator_full.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(XMLManipulator_full.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try (PrintWriter out3 = new PrintWriter("system2_Smarts_Time.txt")){
            try (PrintWriter out2 = new PrintWriter("system2_Smarts_Time.txt")){   
                try (PrintWriter out = new PrintWriter("system2_X_p1.0_s0.5.txt")) {    

                List<Double> stepTimes = new ArrayList<Double>(table.keySet());
                Collections.sort(stepTimes);
                
                //my mods
                //gera um mapa com infos de cada veiculo: tempo total de viagem POR ARQUIVO DE SIMULAÇÃO
                /*HashMap<String, HashMap<String,Double>> vehiclesTotalTimePerFile = new HashMap<>();
                
                for(String vehicleID : vehiclesTable.keySet()){
                    for(String fileName : vehiclesTable.get(vehicleID).keySet()){
                        List<Double> timeList = vehiclesTable.get(vehicleID).get(fileName);
                        Collections.sort(timeList);
                        
                        Double totalTime = timeList.get(timeList.size()-1) - timeList.get(0);
                        
                        vehiclesTotalTimePerFile.put(vehicleID, new HashMap<>());
                        vehiclesTotalTimePerFile.get(vehicleID).put(fileName, totalTime);
                    }
                }*/ 
                //tenho q gerar 3 pra cada file: medioSmarts, medioGeral, medioNonSmarts

                List<Double> smartcarsMeanTravelTimePerStep = new ArrayList<>();
                List<Double> nonSmartcarsMeanTravelTimePerStep = new ArrayList<>();
                List<Double> allCarsMeanTravelTimePerStep = new ArrayList<>();
                
                for(int t=0; t<stepTimes.size();t++){
                    DescriptiveStatistics dsSmarts = new DescriptiveStatistics();
                    DescriptiveStatistics dsNonSmarts = new DescriptiveStatistics();
                    DescriptiveStatistics dsGeral = new DescriptiveStatistics();
                    
                    for(File file : files){
                    List<List<Double>> allTogether = generateValueVectorBasedOnSteps(stepTimes,vehiclesTable, file.getName(),smartcarsIDsTable.get(file.getName()));
                    //for(){}
                    //adiciona o step 't' de todos os files
                    dsSmarts.addValue(allTogether.get(0).get(t));
                    dsNonSmarts.addValue(allTogether.get(1).get(t));
                    dsGeral.addValue(allTogether.get(2).get(t));
                    /*DescriptiveStatistics dsSmartsPerFile = new DescriptiveStatistics();
                    DescriptiveStatistics dsNonSmartsPerFile = new DescriptiveStatistics();
                    DescriptiveStatistics dsGeralPerFile = new DescriptiveStatistics();
                
                    for(String vehicleID : vehiclesTotalTimePerFile.keySet()){
                        if(vehiclesTotalTimePerFile.get(vehicleID).containsKey(file.getName())){
                            double tempVehicleTotalTime = vehiclesTotalTimePerFile.get(vehicleID).get(file.getName());
                        
                            if( smartcarsIDs.contains(vehicleID)){
                                dsSmartsPerFile.addValue(tempVehicleTotalTime);
                                dsGeralPerFile.addValue(tempVehicleTotalTime);
                            }else{
                                dsNonSmartsPerFile.addValue(tempVehicleTotalTime);
                                dsGeralPerFile.addValue(tempVehicleTotalTime);
                            }
                        }
                    }
                //print ds.getMean //me da o tempo medio das coisas pra cada file
                    //out.printf("%.5f\r\n", dsSmartsPerFile.getMean()); //tempo medio de todos os Smarts POR FILE
                    dsSmarts.addValue(dsSmartsPerFile.getMean());
                    dsNonSmarts.addValue(dsNonSmartsPerFile.getMean());
                    dsGeral.addValue(dsGeralPerFile.getMean());
                */
                }
                    //adiciona ordenadamente por timeStep o tempo médio de viagem pra cada categoria por CONDIÇÃO ('p' e 's')
                smartcarsMeanTravelTimePerStep.add(dsSmarts.getMean());
                nonSmartcarsMeanTravelTimePerStep.add(dsNonSmarts.getMean());
                allCarsMeanTravelTimePerStep.add(dsGeral.getMean());
                
                }
                
                for(int t=0; t<stepTimes.size(); t++){
                    out.printf("%.5f\r\n", smartcarsMeanTravelTimePerStep.get(t)); //smarts
                    out2.printf("%.5f\r\n", nonSmartcarsMeanTravelTimePerStep.get(t)); //nonSmarts
                    out3.printf("%.5f\r\n", allCarsMeanTravelTimePerStep.get(t)); //all
                }
                //end my mods
                
                
                    /*for (Double stepTime : stepTimes) {
                    //out.printf("\"Step %.5f\"", stepTime);
                        out.printf("%.5f\r\n", stepTime);
                        DescriptiveStatistics ds = new DescriptiveStatistics();
                        for (File file : files) {
                            double value;
                            if (table.get(stepTime).containsKey(file.getName())) {
                                value = Double.parseDouble(table.get(stepTime).get(file.getName()));
                            
                                if(value < 0.0){
                                
                                    ds.addValue(0.0);
                            
                                }else{
                                    ds.addValue(value);
                            
                                }
                            
                           
                         //   out.printf(",\"%.5f\"", value);
                            } else {
                        //    out.print(",\"\"");
                            }
                        }
                   // out.printf(",\"%.5f\",\"%.5f\"\r\n", ds.getMean(), ds.getStandardDeviation());
                  
                        out2.printf("%.5f\r\n", ds.getMean());
                   
                    
                    }*/
            }}} 
            catch (FileNotFoundException ex) {
                    Logger.getLogger(XMLManipulator_full.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    private static List<String> readFile(String filename){
        List<String> records = new ArrayList<String>();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null){
              records.add(line);
            }
            reader.close();
            return records;
        }
        catch (Exception e){
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }
    
    
    public static List<List<Double>> generateValueVectorBasedOnSteps(List<Double> timeSteps, HashMap<String, HashMap<String,List<Double>>> vehiclesTable, String fileName, List<String> smartcarsIDs){
        List<Double> smartcarsMeanTravelTimePerStep = new ArrayList<>();
        List<Double> nonSmartcarsMeanTravelTimePerStep = new ArrayList<>();
        List<Double> allCarsMeanTravelTimePerStep = new ArrayList<>();
        
        List<List<Double>> allTogether = new ArrayList<>();
        
        for(Double timeStep : timeSteps){ //em ordem crescente
            DescriptiveStatistics dsSmartsPerFile = new DescriptiveStatistics();
            DescriptiveStatistics dsNonSmartsPerFile = new DescriptiveStatistics();
            DescriptiveStatistics dsGeralPerFile = new DescriptiveStatistics();
                
            for(String vehicleID : vehiclesTable.keySet()){
                List<Double> timeList = vehiclesTable.get(vehicleID).get(fileName);
                Collections.sort(timeList);
                Collections.reverse(timeList); //ordenado do maior pro menor, ou seja, da saida pra entrada
                
                if(timeStep >= timeList.get(0)){
                    if( smartcarsIDs.contains(vehicleID)){
                        //adiciona o tempo total de viagem de cada carro q ja terminou de viajar
                        dsSmartsPerFile.addValue(timeList.get(0) - timeList.get(timeList.size() -1));
                        dsGeralPerFile.addValue(timeList.get(0) - timeList.get(timeList.size() -1));
                    }
                    else{
                        dsNonSmartsPerFile.addValue(timeList.get(0) - timeList.get(timeList.size() -1));
                        dsGeralPerFile.addValue(timeList.get(0) - timeList.get(timeList.size() -1));
                    }
                }
            }
            smartcarsMeanTravelTimePerStep.add(dsSmartsPerFile.getMean());
            nonSmartcarsMeanTravelTimePerStep.add(dsNonSmartsPerFile.getMean());
            allCarsMeanTravelTimePerStep.add(dsGeralPerFile.getMean());            
        }
        
        //os vetores devem estar carregados com os tempos médios de viagem de cada categoria
        
        allTogether.add(smartcarsMeanTravelTimePerStep);
        allTogether.add(nonSmartcarsMeanTravelTimePerStep);
        allTogether.add(allCarsMeanTravelTimePerStep);
        
        return allTogether;
    }
    
    public boolean mountSmartcarsIDsTable(String model, String p, String s){
        for(int i=1; i <11;i++){
            String key = model+"."+p+"."+s+"." +i +"."+".Smarts.txt";
            smartcarsIDsTable.put(key,readFile(key));
        }  
        return true;
    } 
    
}
