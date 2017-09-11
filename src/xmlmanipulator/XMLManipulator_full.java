/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlmanipulator;

import java.io.File;
import java.io.FileNotFoundException;
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
    public static void main(String[] args) throws FileNotFoundException {
        
        File dir = new File("data");
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLManipulator_full.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        HashMap<Double, HashMap<String, String>> table = new HashMap<>();
        HashMap<Integer, HashMap<String,List<Double>>> vehiclesTable = new HashMap<>(); //id do smart e lista com timesteps de entrada ate saida
        
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
                            Integer id = Integer.parseInt(vehicle.getAttribute("id"));
                            
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

            try (PrintWriter out2 = new PrintWriter("system2_Smarts_Time.txt")){   
            try (PrintWriter out = new PrintWriter("system2_X_p1.0_s0.5.txt")) {    

            List<Double> stepTimes = new ArrayList<Double>(table.keySet());
            Collections.sort(stepTimes);
                
                //my mods
                //gera um mapa com infos de cada veiculo: tempo total de viagem POR ARQUIVO DE SIMULAÇÃO
            HashMap<Integer, HashMap<String,Double>> vehiclesTotalTimePerFile = new HashMap<>();
                
            for(Integer vehicleID : vehiclesTable.keySet()){
                for(String fileName : vehiclesTable.get(vehicleID).keySet()){
                    List<Double> timeList = vehiclesTable.get(vehicleID).get(fileName);
                    Collections.sort(timeList);
                    Double totalTime = timeList.get(timeList.size()-1) - timeList.get(0);
                    vehiclesTotalTimePerFile.put(vehicleID, new HashMap<>());
                    vehiclesTotalTimePerFile.get(vehicleID).put(fileName, totalTime);
                }
            } 
                //tenho q gerar 3 pra cada file: medioSmarts, medioGeral, medioNonSmarts

            for(File file : files){
                for(Integer vehicleID : vehiclesTotalTimePerFile.keySet()){
                    if(vehiclesTotalTimePerFile.get(vehicleID).containsKey(file.getName())){
                        double tempVehicleTotalTime = vehiclesTotalTimePerFile.get(vehicleID).get(file.getName());
                        
                        if(true /* se o ID tiver na lista de smarts*/){
                            //dsSmarts.addValue(tempVehicleTotalTime);
                            //dsGeral.addValue(tempVehicleTotalTime);
                        }else{
                            //dsNonSmarts.addValue(tempVehicleTotalTime);
                            //dsGeral.addValue(tempVehicleTotalTime);
                        }
                    }
                }
                //print ds.getMean //me da o tempo medio das coisas pra cada file
                
                //parei aqui!!!!!!!!!!!!!!!!!!
            }
                
                //end my mods
                
                
                for (Double stepTime : stepTimes) {
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
                   
                    
                }
            }} catch (FileNotFoundException ex) {
                Logger.getLogger(XMLManipulator_full.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
