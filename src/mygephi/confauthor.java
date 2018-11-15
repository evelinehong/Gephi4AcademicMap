package mygephi;
import myclass.HttpRequest;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;  
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.awt.Color;
import static mygephi.confauthormap.capitalize;
import static mygephi.confauthormap.generateGml;
import static mygephi.confauthormap.generateSvg;
import static mygephi.confauthormap.getValue;
import static mygephi.confauthormap.toHexFromColor;


import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.layout.plugin.noverlap.NoverlapLayout;
import org.gephi.layout.plugin.noverlap.NoverlapLayoutBuilder;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;
import org.gephi.statistics.plugin.Modularity;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;



public class confauthor {
        public static int isStop = 1;
	private static Statement stmt = null, stmt2 = null, stmt3 = null;   //Statement是一个接口，提供了向数据库发送执行语句和获取结果的方法
	//private static Statement stmt2 = null;  //ResultSet是接口是用来创建存放从数据库里得到的结果的对象
	private static Connection conn = null;  //Connection是用于将JAVA和数据库连接的类
        private static Color[] colors=new Color[]{new Color(0xFF0000), new Color(0xFCA2CD), new Color(0x0426E2)};//~用来设置color的RGB值
        private static float[] positions={0f, 0.50f, 1f};
    
        public static Color getValue(float pos) {
            for (int a = 0; a < positions.length - 1; a++) {
                if (positions[a] == pos) {
                    return colors[a];
                }
                if (positions[a] < pos && pos < positions[a + 1]) {
                    float v = (pos - positions[a]) / (positions[a + 1] - positions[a]);
                    return tween(colors[a], colors[a + 1], v);
                }
            }
            if (pos <= positions[0]) {
                return colors[0];
            }
            if (pos >= positions[positions.length - 1]) {
                return colors[colors.length - 1];
            }
            return null;
        }
        private static int hIndex(Integer[] arr) {
        int len = arr.length;
        int[] counts = new int[len + 1];
        int countsSoFar = 0;

        for (int i = 0; i < len; i++) {
            counts[Math.min(arr[i], len)]++;
        }

        for (int i = counts.length - 1; i >= 0; i--) {
            countsSoFar += counts[i];

            if (countsSoFar >= i) {
                return i;
            }
        }

        return 0;
    }

        private static Color tween(Color c1, Color c2, float p) {
            return new Color(
                    (int) (c1.getRed() * (1 - p) + c2.getRed() * (p)),
                    (int) (c1.getGreen() * (1 - p) + c2.getGreen() * (p)),
                    (int) (c1.getBlue() * (1 - p) + c2.getBlue() * (p)),
                    (int) (c1.getAlpha() * (1 - p) + c2.getAlpha() * (p)));
        } 
        
        
        
	public static String capitalize(String str) {
		StringBuffer sbn=new StringBuffer(str);
		StringBuffer ss=new StringBuffer("");
		String s=sbn.toString();
		String[] sb=s.split(" ");
		for(int i=0;i<sb.length;i++){
			sb[i]=sb[i].substring(0,1).toUpperCase()+sb[i].substring(1); 
		}
		for(int i=0;i<sb.length;i++){
		   ss.append(sb[i]);
		   ss.append(" ");
		}
		return ss.toString();
	}
		
	public static Color[] generateColors(Collection values) {
		int maxi = 1000;
		int mini = 3000;
		int sz = values.size();
		Object[] arr = values.toArray();
		int result[] = new int[sz];
		Color[] colorsNew = new Color[sz];
		for (int i = 0; i < sz; ++i) {
			int value = (int)((Double)arr[i]).doubleValue();
			value = Math.min(2016, value);
			value = Math.max(1986, value);      //限制年份在1986-2016
			maxi = Math.max(value, maxi);       //保存最大年份
			mini = Math.min(value, mini);       //保存最小年份
			result[i] = value;
		}
		int mid = (maxi + mini) / 2;
		for (int i = 0; i < sz; ++i) {
                    colorsNew[i]=getValue((float)(1.0*(result[i]-mini)/(maxi-mini)));//~颜色是用年份来区分的
		}
		return colorsNew;
	}

        public static String toHexFromColor(Color color){  
        String r,g,b;  
        StringBuilder su = new StringBuilder();  
        r = Integer.toHexString(color.getRed());  
        g = Integer.toHexString(color.getGreen());  
        b = Integer.toHexString(color.getBlue());  
        r = r.length() == 1 ? "0" + r : r;  
        g = g.length() ==1 ? "0" +g : g;  
        b = b.length() == 1 ? "0" + b : b;  
        r = r.toUpperCase();  
        g = g.toUpperCase();  
        b = b.toUpperCase();  
        //su.append("0xFF");  
        su.append(r);  
        su.append(g);  
        su.append(b);  
        //0xFF0000FF  
        return su.toString();  
    }
        
  
	
	public static void generateGml(String ConferenceID) {
		try{
            //String sql = "select * from ConferenceSeries WHERE ConferenceSeriesID = '" + fieldOfStudyID + "'";    //要执行的SQL
            String sql = "SELECT * FROM ConferenceSeries WHERE ConferenceSeriesID='" + ConferenceID + "'";
            ResultSet rs = stmt.executeQuery(sql);
            int numberOfAuthors = 200; //number of papers to show before filtered
            //double delta = 0.4;
            int totalAmountOfAuthors = 0;
            String ConferenceName = ConferenceID;
            int qualified = 0;
            /*while (rs.next()){
            	totalAmountOfAuthors = rs.getInt("TotalAmountOfAuthors");
            	//fieldOfStudyName = rs.getString("FieldsOfStudyName");
                qualified = rs.getInt("AuthorsQualified");
            	//numberOfPapers = Math.min((int) Math.min(totalAmountOfPapers, 5 * Math.sqrt(totalAmountOfPapers)), numberOfPapers);
                //numberOfPapers = Math.min(totalAmountOfPapers, numberOfPapers);
            }
            rs.close();
            
            if (qualified > numberOfAuthors*delta && totalAmountOfAuthors>numberOfAuthors) {
                qualified = (int) (numberOfAuthors*delta);
            }
            numberOfAuthors = Math.min(totalAmountOfAuthors, numberOfAuthors);
            
            System.out.println("(" + numberOfAuthors + "/" + qualified + ")" + " authors out of " + totalAmountOfAuthors + " in the conference of " + ConferenceName + "(" + ConferenceID + ") will be displayed.");*/
            
            /*sql =       "(SELECT PaperID,PaperReferenceCount \n" +
                        "FROM \n" +
                        "	(SELECT DISTINCT PaperID FROM PaperKeywords WHERE FieldOfStudyIDMappedToKeyword='"+fieldOfStudyID+"') AS TB1 \n" +
                        "	INNER JOIN PaperReferencesCount2 \n" +
                        "	ON TB1.PaperID = PaperReferenceID \n" +
                        "ORDER BY PaperReferenceCount DESC LIMIT "+numberOfPapers+") \n" +
                        "UNION \n" +
                        "(SELECT DISTINCT PaperID,FieldCitation \n" +
                        "FROM PaperRefKeywords \n" +
                        "WHERE FieldOfStudyIDMappedToKeyword='"+fieldOfStudyID+"' \n" +
                        "ORDER BY FieldCitation DESC LIMIT "+qualified+")";   //PaperID*/
            sql =         /*"SELECT * FROM\n"+
                          "(SELECT AuthorID AS authorid, COUNT(AuthorID) AS counts, NumberOfPaper\n"+
                          "FROM AuthorInfo\n" +
                          "INNER JOIN\n" +*/
                          "SELECT\n" + 
                          "AuthorID, COUNT(AuthorID)AS counts\n" +
                          "FROM\n" +
                          "PaperAuthorAffiliations\n" +
                          "INNER JOIN\n" +
                          "(SELECT \n" +
                          "Papers.PaperID\n" +
                          "FROM \n" +
                          "Papers \n" +
                          "WHERE \n" +
                          "ConferenceSeriesIDMappedToVenueName = '"+ConferenceID+"' \n" +
                          ") AS tb2 ON PaperAuthorAffiliations.PaperID = tb2.PaperID \n" +
                          "GROUP BY AuthorID ORDER BY counts DESC\n" ;
                           /*+
                          "ON authorid=authorid2\n" +
                          "GROUP BY AuthorID, NumberOfPaper ORDER BY counts DESC) AS TB4";*/
   
                          
                    
                          
            rs = stmt.executeQuery(sql);
            System.out.println("Retrieved authors id successfully.");
            
            /*HashMap<String , Integer> hashMap = new HashMap<String , Integer>();
            String id="";*/
            Integer count=0,total=0,count1=0,cnt1=0;
            while (rs.next()) {
                
                    //id = rs.getString("AuthorID");
                    //count = Integer.parseInt(rs.getString("counts"));
                    //count1 = Integer.parseInt(rs.getString("NumberOfPaper"));
                    //count = (int) Math.log(count);
                    
                    /*if(count>1)
                    {cnt1++;
                    hashMap.put(id, count);}*/
                    
                    total++;
                
            }
            
            rs.close(); 
            System.out.println("Retrieved "/*+ cnt1 + " out of "*/+ total + " authors id successfully.");
            qualified = (int)Math.min(1300, Math.sqrt(total)*10);
            qualified = (int)Math.max(qualified, 600);
            OutputStream os2 = null;
             try {
                    os2 = new FileOutputStream(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "--hindex.txt");//创建一个文件路径
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(os2));
            System.out.println(qualified + " authors qualified");
                    sql =         /*"SELECT * FROM\n"+
                          "(SELECT AuthorID AS authorid, COUNT(AuthorID) AS counts, NumberOfPaper\n"+
                          "FROM AuthorInfo\n" +
                          "INNER JOIN\n" +*/
                          "SELECT\n" + 
                          "AuthorID, COUNT(AuthorID)AS counts\n" +
                          "FROM\n" +
                          "PaperAuthorAffiliations\n" +
                          "INNER JOIN\n" +
                          "(SELECT \n" +
                          "Papers.PaperID\n" +
                          "FROM \n" +
                          "Papers \n" +
                          "WHERE \n" +
                          "ConferenceSeriesIDMappedToVenueName = '"+ConferenceID+"' \n" +
                          ") AS tb2 ON PaperAuthorAffiliations.PaperID = tb2.PaperID \n" +
                          "GROUP BY AuthorID ORDER BY counts DESC LIMIT "+qualified+"" ;
                    rs = stmt.executeQuery(sql);
                    HashMap<String , Integer> hashMap = new HashMap<String , Integer>();
                    
                    String id="";
                    while (rs.next()) {
                
                    id = rs.getString("AuthorID");
                    count = Integer.parseInt(rs.getString("counts"));
                    //count1 = Integer.parseInt(rs.getString("NumberOfPaper"));
                    //count = (int) Math.log(count);

                    cnt1++;
                    hashMap.put(id, count);
                  
            }
            rs.close(); 
            System.out.println(cnt1+" authors finnally");
            //qualified=(int)(1/4000*total+1.5);
            
            OutputStream os = null;
            try {
                    os = new FileOutputStream(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "3.gml");//创建一个文件路径
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
             
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            
            writer.write("graph [\n");
            writer.write("directed 1\n");
            
            Integer limit = 10000;
            //Author[] authors = new Author[limit+100];
            
            HashSet<String> hashSet = new HashSet<String>();
            
            Iterator iter = hashMap.entrySet().iterator();//map.entrySet()是把HashMap类型的数据转换成集合类型 map.entrySet().iterator()是去获得这个集合的迭代器，保存在iter里面。
            int cnt=0;
            
            int totalprocess=0;
            double numberofpapers;
            double size=0.0;
            int size1 = 0,size2=0;
            //Integer[] hindex = new Integer[3000];
            HashMap<String , Integer> hashMap1 = new HashMap<String , Integer>();
            HashMap<String, Integer>hashMap2 = new HashMap<String,Integer>();
            while (iter.hasNext()) {//若仍有元素可以迭代就返回true
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();//不确定传进来的是什么类就用这个笼统的
                Object val = entry.getValue();
                
                //Integer numberOfPapers1 = (int)Math.min(Integer.parseInt(val.toString()), Integer.parseInt(val.toString())*numberOfPapers/total);
                if (/*(Double.valueOf(val.toString()).doubleValue())>=qualified*/cnt>=0) {
                 cnt++ ;
                //System.out.println(key+"NODE" + val + ": " + ++totalprocess);
                
                /*sql =   "SELECT PaperPublishYear, AuthorName, ChildFieldOfStudyID \n"+
                        "FROM Papers \n"+
                        "INNER JOIN \n"+
                        "   (SELECT Authors.AuthorName, ChildFieldOfStudyID \n"+
                        "   FROM Authors \n"+
                        "   INNER JOIN \n"+
                        "       (SELECT AuthorID, ChildFieldOfStudyID \n"+
                        "       FROM (PaperAuthorAffiliations \n"+
                        "           INNER JOIN \n"+
                        "           (SELECT ChildFieldOfStudyID FROM \n"+
                        "               (SELECT * FROM \n"+
                        "               (SELECT DISTINCT FieldOfStudyIDMappedToKeyword FROM PaperKeywords WHERE PaperID = '" +key + "') AS TB \n"+
                        "               INNER JOIN FieldOfStudyHierarchy \n"+
                        "               ON TB.FieldOfStudyIDMappedToKeyword=ChildFieldOfStudyID WHERE ParentFieldOfStudyID='" + fieldOfStudyID + "' ORDER BY ChildFieldOfStudyLevel)AS TB2 \n"+
                        "           LIMIT 1)AS TB3) \n"+
                        "       WHERE AuthorSequenceNumber = 1 AND PaperAuthorAffiliations.PaperID='" +key + "')AS TB4 \n"+
                        "   ON TB4.AuthorID = Authors.AuthorID)AS TB5 \n"+
                        "ON Papers.PaperID =  '" +key + "'";   //sql2:找到所有子领域的paper信息*/
                /*sql =   "SELECT * FROM \n" +
                        "	((SELECT AuthorName FROM Authors INNER JOIN \n" +
                        "	(SELECT AuthorID,PaperID FROM PaperAuthorAffiliations WHERE AuthorID='"+key+"' ) AS TB1 \n" +
                        "	ON TB1.AuthorID=Authors.AuthorID) AS TB2) \n" +
                        "INNER JOIN \n" +
                        "	(SELECT AuthorID, NumberOfPaper, NumberOfCitation FROM AuthorInfo WHERE AuthorID='"+key+"') AS TB3 ";*/
                sql = "SELECT * FROM\n" +
                      "(SELECT AuthorName FROM Authors\n" +
                      "INNER JOIN\n" +
                      "(SELECT AuthorID FROM AuthorInfo WHERE AuthorID='"+key+"')AS TB1\n" +
                      "ON Authors.AuthorID=TB1.AuthorID)AS TB2";
                
                rs = stmt.executeQuery(sql);
                while(rs.next()) {
                 if ((!hashSet.contains(key.toString()))){
                        int h=0;
                        Integer[] hindex = new Integer[3000];
                         for(int i = 0 ; i < 3000 ; i++) {  
                        hindex[i] = 0 ;  
                        } 
                        size=0.0;size1=0;size2=0;
                        writer.write("node [\n");
                        hashSet.add(key.toString());
                        writer.write("id \"" + key.toString() + "\"\n");
                        String Authorname = capitalize(rs.getString("AuthorName"));
                        writer.write("label \"" + capitalize(rs.getString("AuthorName")) + "\"\n"); 
                 sql = "SELECT * FROM\n" +
                      "((SELECT PaperID, AuthorSequenceNumber\n" +
                      "FROM PaperAuthorAffiliations WHERE AuthorID = '"+key+"')AS TB1\n" +
                      "INNER JOIN \n" +
                      "(SELECT PaperID,CitationCount\n" +
                      "FROM Papers\n" +
                      "WHERE ConferenceSeriesIDMappedToVenueName = '"+ConferenceID+"')AS TB3\n" +
                      "ON TB1.PaperID = TB3.PaperID)";
                          
                 rs = stmt.executeQuery(sql);
                 while(rs.next()) {
                     
                     
                     String citation = rs.getString("CitationCount");
                     //String AuthorSequenceNumber = rs.getString("AuthorSequenceNumber");
                     //double citationcount = (Double.valueOf(citation.toString()).doubleValue())/(Double.valueOf(rs.getString("AuthorSequenceNumber")).doubleValue());
                 
                     //size=size+1+ (Double.valueOf(citation.toString()).doubleValue())/(Double.valueOf(rs.getString("AuthorSequenceNumber")).doubleValue())/200;
                     size1=size1+1;
                     //size2 = size2 + 100+Integer.parseInt(citation.toString())/1000;
                     hindex[h] = Integer.parseInt(citation);
                     size2=size2+Integer.parseInt(citation.toString());
                     //System.out.println(citation+ " " + AuthorSequenceNumber + " " + citationcount + " " + size +" "+size1+" "+ size2+" ");
                     h++;
                 }      int size3 = hIndex(hindex)*100 +size2/100;
                        writer.write("size "+ ""+ size3 + "\n]\n");
                        hashMap1.put(Authorname, size1);
                        hashMap2.put(Authorname,hIndex(hindex));
                        //System.out.println(hIndex(hindex));
                        //writer2.write(Authorname +";"+size1 +"\n");
                        //writer.write("size1 "+ ""+ size1 + "\n");
                        //writer.write("size2 " + ""+ size2 + "\n]\n");
                        
                }
                }
                List<HashMap.Entry<String, Integer>> infoIds =
            new ArrayList<HashMap.Entry<String, Integer>>(hashMap2.entrySet());
               Collections.sort(infoIds, new Comparator<HashMap.Entry<String, Integer>>() {   
                 public int compare(HashMap.Entry<String, Integer> o1, HashMap.Entry<String, Integer> o2) {      
            //return ((int)(o2.getValue() - o1.getValue())); 
                 return (o2.getValue()).compareTo(o1.getValue());
                    }
                }); 
               /*Collections.sort(infoIds, new Comparator<HashMap.Entry<String, Double>>() {  

            public int compare(HashMap.Entry<String, Double> o1, Entry<String, Long> o2) {  
                //return o1.getValue().compareTo(o2.getValue());  
                return o2.getValue().compareTo(o1.getValue());  
            }  
        });  */
               /*for (int i = 0; i < infoIds.size(); i++) {
                String id1 = infoIds.get(i).toString();
                  writer2.write(id1+"\n");
}*/
               for (HashMap.Entry<String, Integer> mapping : infoIds) {  
            writer2.write(mapping.getKey() + ":" + mapping.getValue()+"\n");  
        }
              
                
                
                /*sql = "SELECT * FROM\n" +
                      "((SELECT PaperID, AuthorSequenceNumber\n" +
                      "FROM PaperAuthorAffiliations WHERE AuthorID = '"+key+"')AS TB1\n" +
                      "INNER JOIN \n" +
                      "(SELECT PaperID,CitationCount\n" +
                      "FROM Papers\n" +
                      "WHERE ConferenceSeriesIDMappedToVenueName = '"+ConferenceID+"')AS TB3\n" +
                      "ON TB1.PaperID = TB3.PaperID)";
                          
                 rs = stmt.executeQuery(sql);
                 while(rs.next()) {
                     if ((hashSet.contains(key.toString()))){
                     size=size+1+(Double.valueOf(val.toString()).doubleValue())/(10*(Double.valueOf(rs.getString("AuthorSequenceNumber"))));
                     }
                 }
                 writer.write("size " + ""+ size + "\n]\n");*/

                 /*if ((!hashSet.contains(key.toString()))){
                 writer.write("node [\n");
                        hashSet.add(key.toString());
                        writer.write("id \"" + key.toString() + "\"\n");
                        writer.write("label \"" + capitalize(rs.getString("AuthorName")) + "\"\n");
                        
                        //writer.write("reference \"" + val.toString() + "\"\n");
                          //属于多个fields的paper，认为它的field为第一个遍历到它的
                        //writer.write("field \"" + (rs.getString("ChildFieldOfStudyID")) + "\"\n");
                        writer.write("numberofpapers " + rs.getString("NumberOfPaper") + "\n");
                        writer.write("numberofcitation " + rs.getString("NumberOfCitation") + "\n]\n");
                 }*/
                //System.out.println("Retrieved papers successfully.");

                /*while (rs.next()) {
                    if ((!hashSet.contains(key.toString()))&&((Double.valueOf(rs.getString("NumberOfPaper")).doubleValue())<=5000.0))
                    {
                        
                        //papers[cnt] = new Paper();
                        //papers[cnt].id = key.toString();
                        //papers[cnt].field = rs.getString("ChildFieldOfStudyID");
                        writer.write("node [\n");
                        hashSet.add(key.toString());
                        writer.write("id \"" + key.toString() + "\"\n");
                        writer.write("label \"" + capitalize(rs.getString("AuthorName")) + "\"\n");
                        
                        //writer.write("reference \"" + val.toString() + "\"\n");
                          //属于多个fields的paper，认为它的field为第一个遍历到它的
                        //writer.write("field \"" + (rs.getString("ChildFieldOfStudyID")) + "\"\n");
                        writer.write("numberofpapers " + rs.getString("NumberOfPaper") + "\n");
                        writer.write("numberofcitation " + rs.getString("NumberOfCitation") + "\n]\n");

                    }
                }*/
                //System.out.println(cnt+" out of "+total+" authors retrieved");
                
                
 
                } 
               
                        
                /*
                             sql =   "SELECT * FROM \n" +
                        "	(SELECT PaperID FROM PaperAuthorAffiliations INNER JOIN \n" +
                        "	(SELECT AuthorID FROM PaperAuthorAffiliations WHERE AuthorID='"+key+"' AND AuthorSequenceNumber=1) AS TB1 \n" +
                        "	ON TB1.PaperID=Authors.AuthorID) AS TB2 \n" +
                        "INNER JOIN \n" +
                        "	(SELECT AuthorID, NumberOfPaper FROM AuthorInfo WHERE AuthorID='"+key+"') AS TB3 ";
                */
            	 /*sql = 
                        "	(SELECT AuthorID FROM PaperAuthorAffiliations WHERE AuthorID!='"+key+"'） AS TB2 \n" +
                        "        INNER JOIN \n" +
                        "	(SELECT PaperID FROM PaperAuthorAffiliations WHERE AuthorID='"+key+"' AND AuthorSequenceNumber=1) AS TB1 \n" +
                        "	ON TB1.PaperID=TB2.PaperID）" ;*/
                   /*sql = "(SELECT DISTINCT AuthorID, COUNT( AuthorID) AS counts FROM\n "+
                         "PaperAuthorAffiliations\n"+
                         "INNER JOIN\n"+
                         "(SELECT PaperID FROM PaperAuthorAffiliations WHERE AuthorID='"+key+"')AS tb1\n"+
                         "ON PaperAuthorAffiliations.PaperID = tb1.PaperID\n" +
                         "GROUP BY AuthorID \n" +
                         "ORDER BY counts DESC )";
                   rs = stmt.executeQuery(sql);
                   while(rs.next()){
                       String tmp = rs.getString("AuthorID");
                       int val1=rs.getInt("counts");
            		if (hashSet.contains(tmp) && !tmp.equals(key.toString())) {
            			writer.write("edge [\n");
            			writer.write("source \"" + key.toString() + "\"\n");
            			writer.write("target \"" + tmp + "\"\n");
                                writer.write("value \"" + val1 + "\"\n]\n");
            		}
                   }
                    rs.close();*/
                 /*sql = "SELECT PaperID FROM PaperAuthorAffiliations WHERE AuthorID='"+key+"'  \n" ;
                       
                 rs = stmt.executeQuery(sql);
                 while(rs.next()){
                 String key2 = rs.getString("PaperID");
                 
                sql ="SELECT AuthorID FROM PaperAuthorAffiliations WHERE PaperID='"+key2+"' AND AuthorID != '"+key+"'";
                     
 
              /*sql =   "SELECT * FROM \n" +
                        "(SELECT\n" + 
                          "DISTINCT AuthorID, COUNT(*) AS counts\n" +
                          "FROM\n" +
                          "PaperAuthorAffiliations\n" +
                          "INNER JOIN\n" +
                          "(SELECT \n" +
                          "Papers.PaperID, PaperRank \n" +
                          "FROM \n" +
                          "Papers \n" +
                          "WHERE \n" +
                          "ConferenceSeriesIDMappedToVenueName = '"+ConferenceID+"' \n" +
                          " LIMIT 5000) AS tb2 ON PaperAuthorAffiliations.PaperID = tb2.PaperID  \n" +
                          "GROUP BY AuthorID \n" +
                          "ORDER BY counts DESC)AS tb3 \n"+
                       "INNER JOIN\n" +
                       "(SELECT DISTINCT AuthorID FROM PaperAuthorAffiliations WHERE PaperID='"+key2+"' )AS tb4 \n" ;
              
    
                      

               
 
            	rs = stmt.executeQuery(sql); 
                
            	while (rs.next()) {
                        
            		String tmp = rs.getString("AuthorID");
            		if (hashSet.contains(tmp) && !tmp.equals(key.toString())) {
            			writer.write("edge [\n");
            			writer.write("source \"" + key.toString() + "\"\n");
            			writer.write("target \"" + tmp + "\"\n]\n");
                                //writer.write("value \"" + tmp1 + "\"\n]\n");
            		}
            	}
            	//rs.close();
 
      
            }
            rs.close();*/
                
            }    writer2.close();
               os2.close();
             System.out.println(cnt +" out of "+total+" authors retrieved");
            /*Iterator<String> iterator= hashSet.iterator();
            while (iterator.hasNext())
            { 
                String key1 = iterator.next();
                System.out.println(key1+"EDGE: "+ ++totalprocess);
               sql = "(SELECT DISTINCT AuthorID, COUNT( AuthorID) AS counts FROM\n "+
                         "PaperAuthorAffiliations\n"+
                         "INNER JOIN\n"+
                         "(SELECT PaperID FROM PaperAuthorAffiliations WHERE AuthorID='"+key1+"')AS tb1\n"+
                         "ON PaperAuthorAffiliations.PaperID = tb1.PaperID\n" +
                         "GROUP BY AuthorID HAVING counts>1 \n" +
                         "ORDER BY counts DESC )";
                   rs = stmt.executeQuery(sql);
                   while(rs.next()){
                       String tmp = rs.getString("AuthorID");
                       int val1=rs.getInt("counts");
            		if (hashSet.contains(tmp) && !tmp.equals(key1.toString())) {
            			writer.write("edge [\n");
            			writer.write("source \"" + key1.toString() + "\"\n");
            			writer.write("target \"" + tmp + "\"\n");
                                writer.write("value " + val1 + "\n]\n");
            		}
                   }
                    rs.close();
            }
            /*  Iterator<String> iterator= hashSet.iterator();
            
       
            }*/
         /*  Iterator<String> iterator= hashSet.iterator();
            
            while (iterator.hasNext())
            {
            	String paperID = iterator.next();
            	
            	sql = "SELECT PaperReferenceID FROM PaperReferences WHERE PaperID = '" + paperID + "'";     //找到每篇paper的引用
            	rs = stmt.executeQuery(sql); 
            	while (rs.next()) {
            		String tmp = rs.getString("PaperReferenceID");
            		if (hashSet.contains(tmp) && !tmp.equals(paperID)) {
            			writer.write("edge [\n");
            			writer.write("source \"" + paperID + "\"\n");
            			writer.write("target \"" + tmp + "\"\n]\n");
            		}rs = stmt.executeQuery(sql); 
            	}
            	rs.close();
            }*/
        
//            sql = "SELECT PaperReferenceID, TB5.PaperID FROM PaperReferences INNER JOIN (\n" +
//                        "(SELECT PaperID \n" +
//                        "FROM \n" +
//                        "	(SELECT DISTINCT PaperID FROM PaperKeywords WHERE FieldOfStudyIDMappedToKeyword='"+fieldOfStudyID+"') AS TB1 \n" +
//                        "	INNER JOIN PaperReferencesCount2 \n" +
//                        "	ON TB1.PaperID = PaperReferenceID \n" +
//                        "ORDER BY PaperReferenceCount DESC LIMIT "+numberOfPapers+") \n" +
//                        "UNION \n" +
//                        "(SELECT DISTINCT PaperID \n" +
//                        "FROM PaperRefKeywords \n" +
//                        "WHERE FieldOfStudyIDMappedToKeyword='"+fieldOfStudyID+"' \n" +
//                        "ORDER BY FieldCitation DESC LIMIT "+qualified+")) AS TB5 \n" +
//                "  ON PaperReferences.PaperID = TB5.PaperID";
//            rs = stmt.executeQuery(sql);
//            System.out.println("Retrieved references successfully");
//            while (rs.next()) {
//                String tmp1 = rs.getString("PaperReferenceID");
//                String tmp2 = rs.getString("PaperID6");
//                if (hashSet.contains(tmp1) && hashSet.contains(tmp2) && !tmp1.equals(tmp2)) {
//                    writer.write("edge [\n");
//                    writer.write("source \"" + tmp2 + "\"\n");
//                    writer.write("target \"" + tmp1 + "\"\n]\n");
//                }
//            }
            rs.close();
            double minweight=1.0;
            String[] array3 = new String[3000];
            String[] array4 = new String[3000];
            String[] array5 = new String[3000];
            String[] array6 = new String[3000];
            String[] array7 = new String[3000];
            String[] array8 = new String[3000];
            int cnt4=0,cnt5=0;
            //if(total>=8000) minweight=2.0;
           
            
            /*if(total<=2000)
            {
            String post = "";
            for(Iterator it=hashSet.iterator();it.hasNext();) {
                post += it.next();
                post += ",";
            }
           String[] results = HttpRequest.sendPost("http://202.120.36.28:10081",post).split(";");
    
            System.out.println(results);
            for (String result : results) {
                String tmp1 = result.split(",")[0];
                String tmp2 = result.split(",")[1];
                String tmp3 = result.split(",")[2];
            if ((!tmp1.equals(tmp2))&&((Double.valueOf(tmp3).doubleValue())>=minweight)) {
                    System.out.println("EDGE: "+ ++totalprocess);
                    writer.write("edge [\n");
                    writer.write("source \"" + tmp1 + "\"\n");
                    writer.write("target \"" + tmp2 + "\"\n");
                    writer.write("value " + tmp3 + "\n]\n");}
            }
            }
            else
            {*/
            String post = ConferenceName+";";
            for(Iterator it=hashSet.iterator();it.hasNext();) {
                post += it.next();
                post += ",";
            }
           String[] results = HttpRequest.sendPost("http://202.120.36.28:10081",post).split(";");
    
            System.out.println(results);
            for (String result : results) {
                String tmp1 = result.split(",")[0];
                String tmp2 = result.split(",")[1];
                String tmp3 = result.split(",")[2];
                if  ((!tmp1.equals(tmp2))&&((Double.valueOf(tmp3).doubleValue())==1.0))
                {array3[cnt4]=tmp1;
                 array4[cnt4]=tmp2;
                 array5[cnt4]=tmp3;
                 cnt4++;}
                
                else
                {array6[cnt5]=tmp1;
                 array7[cnt5]=tmp2;
                 array8[cnt5]=tmp3;
                 cnt5++;} }
                System.out.println(cnt4 + " edge 1.0");
                System.out.println(cnt5 + " edge not 1.0");
                for(int i=0; i<cnt5; i++)
                {   //System.out.println("EDGE: "+ ++totalprocess);
                    writer.write("edge [\n");
                    writer.write("source \"" + array6[i] + "\"\n");
                    writer.write("target \"" + array7[i] + "\"\n");
                    writer.write("value " + array8[i] + "\n]\n");
                }
                
                if((((cnt4+cnt5)<=2*cnt)&&(cnt5<cnt))||(cnt5<0.9*cnt))
                {
                for(int j=0; j<cnt4; j++)
                { 
                   //System.out.println("EDGE: "+ ++totalprocess);
                    writer.write("edge [\n");
                    writer.write("source \"" + array3[j] + "\"\n");
                    writer.write("target \"" + array4[j] + "\"\n");
                    writer.write("value " + array5[j] + "\n]\n");
                }
                }
                    
            
                
                /*if ((!tmp1.equals(tmp2))&&((Double.valueOf(tmp3).doubleValue())>=minweight)) {
                    System.out.println("EDGE: "+ ++totalprocess);
                    writer.write("edge [\n");
                    writer.write("source \"" + tmp1 + "\"\n");
                    writer.write("target \"" + tmp2 + "\"\n");
                    writer.write("value " + tmp3 + "\n]\n");*/

                
            
           
            writer.write("]");
            writer.close();
            os.close();
            
            System.out.println("Content written to " + ConferenceID + "3.gml");
             OutputStream os1 = null;
             
            try {
                    os1 = new FileOutputStream(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "2.txt");//创建一个文件路径
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
           
            String[] array = new String[6000];
            String[] array1 = new String[6000];
            int cnt2=0;
            int groupmember = Math.min(10,(total-qualified)/qualified);
            groupmember=Math.max(groupmember, 2);
            int qualified1=(total-qualified)/groupmember;
            int unqualified = total - qualified - qualified1;
            System.out.println(qualified1+"left");
            BufferedWriter writer1 = new BufferedWriter(new OutputStreamWriter(os1));
            sql =         /*"SELECT * FROM\n"+
                          "(SELECT AuthorID AS authorid, COUNT(AuthorID) AS counts, NumberOfPaper\n"+
                          "FROM AuthorInfo\n" +
                          "INNER JOIN\n" +*/
                        
                          "SELECT AuthorName,TB5.AuthorID FROM Authors INNER JOIN\n" + 
                          "(SELECT\n" + 
                          "AuthorID, COUNT(AuthorID)AS counts\n" +
                          "FROM\n" +
                          "PaperAuthorAffiliations\n" +
                          "INNER JOIN\n" +
                          "(SELECT \n" +
                          "Papers.PaperID\n" +
                          "FROM \n" +
                          "Papers \n" +
                          "WHERE \n" +
                          "ConferenceSeriesIDMappedToVenueName = '"+ConferenceID+"' \n" +
                          ") AS tb2 ON PaperAuthorAffiliations.PaperID = tb2.PaperID \n" +
                          "GROUP BY AuthorID ORDER BY counts DESC LIMIT "+qualified+","+qualified1+")AS TB5\n" +
                          "ON Authors.AuthorID = TB5.AuthorID";
                    
            
            rs = stmt.executeQuery(sql);
                 while(rs.next()) {
                    array[cnt2]=rs.getString("AuthorID");
                    array1[cnt2]=rs.getString("AuthorName");
                    cnt2++;
                    //System.out.println(array[cnt2] + "qualified");
                    //writer1.write("\n"+array[cnt2]+";");
                    //writer1.write(array1[cnt2] + " ");
                    //System.out.println(array[cnt2] + "qualified");
//writer1.write(rs.getString("AuthorID")+ "\n");
                    //System.out.println(array[cnt2] + "qualified");
                 }
                
            //System.out.println(cnt2 + " written to txt");
                    sql = "SELECT AuthorName,TB5.AuthorID FROM Authors INNER JOIN\n" + 
                          "(SELECT\n" + 
                          "AuthorID, COUNT(AuthorID)AS counts\n" +
                          "FROM\n" +
                          "PaperAuthorAffiliations\n" +
                          "INNER JOIN\n" +
                          "(SELECT \n" +
                          "Papers.PaperID\n" +
                          "FROM \n" +
                          "Papers \n" +
                          "WHERE \n" +
                          "ConferenceSeriesIDMappedToVenueName = '"+ConferenceID+"' \n" +
                          ") AS tb2 ON PaperAuthorAffiliations.PaperID = tb2.PaperID \n" +
                          "GROUP BY AuthorID ORDER BY counts LIMIT "+unqualified+")AS TB5\n" +
                          "ON Authors.AuthorID = TB5.AuthorID";
            int cnt3=0;
            rs = stmt.executeQuery(sql);
                while(rs.next()){
                    if((cnt3%(groupmember-1))==0) {
                        //System.out.println(array[cnt3/(groupmember-1)] + "qualified");
                        writer1.write("\n"+array[cnt3/(groupmember-1)]+";");
                        writer1.write(array1[cnt3/(groupmember-1)] + "; ");
                    }
                    writer1.write(rs.getString("AuthorID") + ";");
                    writer1.write(rs.getString("AuthorName") + "; ");
                    cnt3++;
                }
            //System.out.println(cnt3/1 + "groups");
     
            rs.close();
            writer1.close();
            
            os1.close();
            
            
       } catch(Exception e) {
    	   e.printStackTrace();

       }
          
        
	}
	
	public static void generateSvg(String ConferenceID, int mod) throws IOException, SQLException {
		/*File file = new File(System.getProperty("user.dir") + "/papermaps/" + fieldOfStudyID + ".php");
		if (file.exists()) {
			System.out.println(fieldOfStudyID + " already exists");
			return;
		}*/
                isStop = 1;
		generateGml(ConferenceID);
		
		//Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();  //GraphModel好比存放整个图形元素的容器，包括节点、边、标签等信息
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        //RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
        
        //Import file       
        Container container;
        File gmlFile = new File(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "3.gml");
        try {
            container = importController.importFile(gmlFile);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);   //Force DIRECTED
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);
        //See if graph is well imported
        DirectedGraph graph = graphModel.getDirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        //Filter      
        DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
        degreeFilter.init(graph);
        degreeFilter.setRange(new Range(1, Integer.MAX_VALUE));     //Remove nodes with degree = 0
        Query query = filterController.createQuery(degreeFilter);
        GraphView view = filterController.filter(query);
        graphModel.setVisibleView(view);    //Set the filter result as the visible view
        //System.out.println("NodesFilter: " + graph.getNodeCount());
        //System.out.println("EdgesFilter: " + graph.getEdgeCount());
        
        //filterController.exportToNewWorkspace(query);
        Graph result = graphModel.getGraph(view);
        Workspace newWorkspace = pc.newWorkspace(pc.getCurrentProject());
        //GraphModel newGraphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(newWorkspace);
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(newWorkspace);
        graphModel.bridge().copyNodes(result.getNodes().toArray());
        pc.openWorkspace(newWorkspace);
        
        //System.out.println(pc.getCurrentWorkspace());
        //FilterControllerImpl.java
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();        //PreviewModel等于获得了一个如何进行预览展现的操作入口        
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();
        
        //See visible graph stats
        UndirectedGraph graphVisible = graphModel.getUndirectedGraphVisible();
        //System.out.println("Nodes: " + graphVisible.getNodeCount());
        //System.out.println("Edges: " + graphVisible.getEdgeCount());
        System.out.println("NodesFilter: " + graph.getNodeCount());
        System.out.println("EdgesFilter: " + graph.getEdgeCount());
        int nodecount=graph.getNodeCount();
        if (graphVisible.getNodeCount() == 0) {
        	//System.out.println("No node!");
        	gmlFile.delete();
        	return;
        }
        
        //System.out.println(pc.getCurrentWorkspace());
        
        /*//Add indgree column
        Column referenceCol = graphModel.getNodeTable().getColumn("reference");
        //Add node size column
        Column yearCol = graphModel.getNodeTable().getColumn("year");
        Column nodeSizeCol = graphModel.getNodeTable().addColumn("Size", Double.class);
        Double year,nodereference,nodeSize;
        for (Node n : graphModel.getDirectedGraph().getNodes()) { 
            year = Double.parseDouble(n.getAttribute(yearCol).toString());
            nodereference = Double.parseDouble(n.getAttribute(referenceCol).toString());
            nodeSize = Math.sqrt(0.5 * nodereference + 0.5 * nodereference * Math.exp((year-2015)/15));
            n.setAttribute(nodeSizeCol, nodeSize);
        }*/
        //Add indgree column

        Column indgreeCol = graphModel.getNodeTable().addColumn("InDgree", Integer.class);
        for (Node n : graphModel.getDirectedGraph().getNodes()) {
            n.setAttribute(indgreeCol, graphModel.getDirectedGraph().getInDegree(n));

        }
        //Add node size column
        List<String> list = new ArrayList();
        Column nodeSizeCol = graphModel.getNodeTable().getColumn("size");
        //Column nodeSizeCol1 = graphModel.getNodeTable().getColumn("size1");
        //Column nodeSizeCol2 = graphModel.getNodeTable().getColumn("size2");
        //Column noCol1 = graphModel.getNodeTable().getColumn("numberofcitation");
        //Column nodeSizeCol = graphModel.getNodeTable().addColumn("Size", Double.class);
        //Column Color = graphModel.getNodeTable().getColumn("numberofcitation");
        Column id = graphModel.getNodeTable().getColumn("id");
        Double numberofpapers,numberofcitation,nodeIndgree,nodeSize,size,size1,size2,nodeSize1,nodeSize2;
        double color;
        Double maxsize=0.0, minsize=1000.0;
        Double maxsize1=0.0, minsize1=1000.0;
        Double maxsize2=0.0, minsize2=1000.0;
        String id2;
        int count2=0;
        for (Node n : graphModel.getDirectedGraph().getNodes()) { 
            id2 = (n.getAttribute(id).toString());
            count2++;
            //System.out.println("test" + id2 + " " + count2);
            size = Double.parseDouble(n.getAttribute(nodeSizeCol).toString());
            if(size>maxsize) maxsize = size;
            if(size<minsize) minsize = size;
            //size1 = Double.parseDouble(n.getAttribute(nodeSizeCol1).toString());
            //System.out.println(size1);
            //size2 = Double.parseDouble(n.getAttribute(nodeSizeCol2).toString());
            //if(size1>maxsize1) maxsize1 = size1;
            //if(size1<minsize1) minsize1 = size1;
            //if(size2>maxsize2) maxsize2 = size2;
            //if(size2<minsize2) minsize2 = size2;
            //numberofcitation = Double.parseDouble(n.getAttribute(noCol1).toString());
            nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
            //nodeSize = 0.5 * nodeIndgree + 0.5 * nodeIndgree * Math.exp((year-2015)/10);
            nodeSize = size;
            //nodeSize1 = size1;
            //nodeSize2 = size2;
            //nodeSize *= nodeSize;
            n.setAttribute(nodeSizeCol, nodeSize);
            //n.setAttribute(nodeSizeCol1, nodeSize1);
            //n.setAttribute(nodeSizeCol2, nodeSize2);
        }
        
        System.out.println("maxsize:" + maxsize);
        System.out.println("minsize:" + minsize);
        System.out.println("maxsize2:" + maxsize2);
        System.out.println("minsize2:" + minsize2);
        System.out.println("maxsize1:" + maxsize1);
        System.out.println("minsize1:" + minsize1);

         
        
            /*color=Double.parseDouble(n.getAttribute(Color).toString());
            if (((int)(color))<=5) n.setColor(new Color(0xFFE4B5));
            else if ((((int)(color))>=5)&&(((int)(color))<=10)) n.setColor(new Color(0xFFC0C8));
            else if ((((int)(color))>=10)&&(((int)(color))<=50)) n.setColor(new Color(0xFFA07A));
            else if ((((int)(color))>=50)&&(((int)(color))<=100)) n.setColor(new Color(0xFF82AB));
            else if ((((int)(color))>=100)&&(((int)(color))<=500)) n.setColor(new Color(0xFF7F00));
            else if ((((int)(color))>=500)&&(((int)(color))<=1000)) n.setColor(new Color(0xFF69B4));
            else if ((((int)(color))>=1000)&&(((int)(color))<=5000)) n.setColor(new Color(0xFF3E96));
            else n.setColor(new Color(0xFF1493));
        }*/
                /*if (((int)(color))<=5) n.setColor(new Color(0xFFC1C1));
            else if ((((int)(color))>=5)&&(((int)(color))<=10)) n.setColor(new Color(0xEEA2AD));
            else if ((((int)(color))>=10)&&(((int)(color))<=50)) n.setColor(new Color(0xFFAEB9));
            else if ((((int)(color))>=50)&&(((int)(color))<=100)) n.setColor(new Color(0xDB7093));
            else if ((((int)(color))>=100)&&(((int)(color))<=500)) n.setColor(new Color(0xFF82AB));
            else if ((((int)(color))>=500)&&(((int)(color))<=1000)) n.setColor(new Color(0xFF69B4));
            else if ((((int)(color))>=1000)&&(((int)(color))<=5000)) n.setColor(new Color(0xFF3E96));
            else n.setColor(new Color(0xFF1493));
        }*/
        

        Column source = graphModel.getEdgeTable().getColumn("source");
        Column target = graphModel.getEdgeTable().getColumn("target");
        Column weightCol = graphModel.getEdgeTable().addColumn("weight2",Double.class);
        Double weight, edgeweight;
        String source1;
        //Set edge weight
        /*Double sourceYear,targetYear;*/
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            source1 = (e.getSource().toString());
            //System.out.println("testedge" + source1 );
            //System.out.println(e.getWeight());
            e.setWeight(Math.log10(e.getWeight())+1.0);
            weight = e.getWeight();
            edgeweight = 20 * weight;
            e.setAttribute(weightCol, edgeweight);
            
            //System.out.println(e.getWeight());
        }
        //System.out.println("1");

           // targetYear = Double.parseDouble(e.getTarget().getAttribute(yearCol).toString());
            /*if (Math.abs(sourceYear - targetYear) < 20) 
                e.setWeight(20 - Math.abs(sourceYear - targetYear)); 
            else
                e.setWeight(1.0);*/
        
        /*Double sourceYear,targetYear;
        String sourceField, targetField;
        Column fieldCol = graphModel.getNodeTable().getColumn("field");
        Double tmpweight;
        int refcnt=0;
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            sourceYear = Double.parseDouble(e.getSource().getAttribute(yearCol).toString());
            targetYear = Double.parseDouble(e.getTarget().getAttribute(yearCol).toString());
            sourceField = e.getSource().getAttribute(fieldCol).toString();
            targetField = e.getTarget().getAttribute(fieldCol).toString();
            if (Math.abs(sourceYear - targetYear) < 20) 
                tmpweight=(20 - Math.abs(sourceYear - targetYear)); 
            else
                tmpweight=1.0;
            if (sourceField.equals(targetField)&& !sourceField.equals(fieldOfStudyID)) {tmpweight=tmpweight*4;refcnt++;}
            e.setWeight(tmpweight);1
        }
        System.out.println(refcnt + "lines are in the same field");*/
       /*  int[] weight = new int[]{50,10,50}; //不同领域引用，同领域新增，同领域引用
        double lineMaxWeight = 20; //线的最大粗细 
        double lineWeight = 0; 
        Column fieldCol = graphModel.getNodeTable().getColumn("field");
        String sourceField, targetField;
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            sourceField = e.getSource().getAttribute(fieldCol).toString();
            targetField = e.getTarget().getAttribute(fieldCol).toString();
            double tmpWeight = (e.getSource().size() + e.getTarget().size()) / 2 - 19;
            if (tmpWeight>lineWeight) lineWeight = tmpWeight;
                       if (sourceField.equals(targetField)) 
            {
                    //double maxWeight = Math.max(Double.parseDouble(e.getTarget().getAttribute(indgreeCol).toString()), Double.parseDouble(e.getSource().getAttribute(indgreeCol).toString()));
                    double avgWeight = (e.getSource().size() + e.getTarget().size()) / 2 - 19;
                    e.setWeight(weight[2] * (avgWeight/lineWeight*lineMaxWeight)); 
                           
            }
            else
            {
                e.setWeight(weight[0]);
            }
                   
                    
                }*/
         
        
      
      
        
        //Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);
        
        /*HashMap<String, Color> yearColor = new HashMap();
        yearColor.put("1", new Color(0x0426E2));
        yearColor.put("2", new Color(0x142EE0));
        yearColor.put("1988.0", new Color(0x2536DF));
        yearColor.put("1989.0", new Color(0x353EDD));
        yearColor.put("1990.0", new Color(0x4647DC));
        yearColor.put("1991.0", new Color(0x564FDB));
        yearColor.put("1992.0", new Color(0x6757D9));
        yearColor.put("1993.0", new Color(0x775FD8));
        yearColor.put("1994.0", new Color(0x8868D6));
        yearColor.put("1995.0", new Color(0x9870D5));
        yearColor.put("1996.0", new Color(0xA978D4));
        yearColor.put("1997.0", new Color(0xB980D2));
        yearColor.put("1998.0", new Color(0xCA89D1));
        yearColor.put("1999.0", new Color(0xDA91CF));
        yearColor.put("2000.0", new Color(0xEB99CE));
        yearColor.put("2001.0", new Color(0xFCA2CD));
        yearColor.put("2002.0", new Color(0xFC97BF));
        yearColor.put("2003.0", new Color(0xFC8CB1));
        yearColor.put("2004.0", new Color(0xFC81A3));
        yearColor.put("2005.0", new Color(0xFC7696));
        yearColor.put("2006.0", new Color(0xFD6B88));
        yearColor.put("2007.0", new Color(0xFD617B));
        yearColor.put("2008.0", new Color(0xFD566D));
        yearColor.put("2009.0", new Color(0xFD4B5F));
        yearColor.put("2010.0", new Color(0xFD4051));
        yearColor.put("2011.0", new Color(0xFE3644));
        yearColor.put("2012.0", new Color(0xFE2B36));
        yearColor.put("2013.0", new Color(0xFE2029));
        yearColor.put("2014.0", new Color(0xFE151B));
        yearColor.put("2015.0", new Color(0xFE0A0D));
        yearColor.put("2016.0", new Color(0xFF0000));*/
        /*if(mod==1){
        Modularity modularity = new Modularity();
        modularity.execute(graphModel);
        Column modColumn = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
        Function colorRanking = appearanceModel.getNodeFunction(graph, modColumn, PartitionElementColorTransformer.class);
        Partition P = ((PartitionFunction) colorRanking).getPartition();
        Object[] partNames = P.getSortedValues().toArray();
        int colorNum = 12;
        Color[] color1 = new Color[colorNum];
        color1[0] = new Color(0xFF8C00);
        color1[1] = new Color(0xF08080);
        color1[2] = new Color(0xEE00EE);
        color1[3] = new Color(0x912CEE);
        color1[4] = new Color(0x1C86EE);
        color1[5] = new Color(0x00BFFF);
        color1[6] = new Color(0x006400);
        color1[7] = new Color(0xADFF2F);
        color1[8] = new Color(0xBF3EFF);
        color1[9] = new Color(0x8B4789);
        color1[10] = new Color(0x4EEE94);
        color1[11] = new Color(0x4169E1);
        System.out.println("Don't!");
        for (int i=0;i<P.size();i++){
            P.setColor(partNames[i],color1[i%colorNum]);//Integer.parseInt(color[Math.min(i%colorNum,colorNum*2-1-i%colorNum)],16)
        }
        appearanceController.transform(colorRanking);}
        
        else
        {Column modColumn = graphModel.getNodeTable().addColumn("ModularityClass",String.class);
        String MOD,mod1 = " ";
            for (Node n : graphModel.getDirectedGraph().getNodes()) {
          n.setColor(new Color(0x191970));
          MOD = mod1;
          n.setAttribute(modColumn, MOD);
        }
        }*/
        
        

        //Column colorCol = graphModel.getNodeTable().getColumn("fill");
        //Column nodeColorCol = graphModel.getNodeTable().addColumn("fill2", String.class);
        /*if (false){
            Column size1Col = graphModel.getNodeTable().getColumn("lsize");
            Column nodeSizeCol = graphModel.getNodeTable().addColumn("Size", Double.class);
            Double size1, nodeIndgree, nodeSize;
            for (Node n : graphModel.getDirectedGraph().getNodes()) {
                size1 = Double.parseDouble(n.getAttribute(size1Col).toString());
                nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                nodeSize = size1;
                n.setAttribute(nodeSizeCol, nodeSize);
            }
        }*/
        /*if (true){
            //Column colorCol = graphModel.getNodeTable().getColumn("fill");
            Column nodeColorCol = graphModel.getNodeTable().addColumn("fill2", String.class);
            String fill2, nodeColor;
            for (Node n : graphModel.getDirectedGraph().getNodes()) {
                fill2 = toHexFromColor(n.getColor());
                //nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                nodeColor = fill2;
                n.setAttribute(nodeColorCol, nodeColor);
            }
        }
            //Column strokeCol = graphModel.getEdgeTable().getColumn("stroke");
            //Column edgeStrokeCol = graphModel.getNodeTable().addColumn("stroke2", String.class);
        if (true){
            Column strokeCol = graphModel.getEdgeTable().getColumn("stroke");
            Column edgeStrokeCol = graphModel.getEdgeTable().addColumn("stroke2", String.class);
            //Column weightCol = graphModel.getEdgeTable().addColumn("weight",Double.class);
            String stroke2, edgeColor;
            for (Edge e : graphModel.getDirectedGraph().getEdges()) {
                if((Double.parseDouble(e.getSource().getAttribute(nodeSizeCol).toString()))>=(Double.parseDouble((e.getTarget().getAttribute(nodeSizeCol)).toString())))
                {stroke2 = toHexFromColor(e.getSource().getColor());}
                else{stroke2 = toHexFromColor(e.getTarget().getColor());}
                //nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                edgeColor = stroke2;
                e.setAttribute(edgeStrokeCol, edgeColor);
            }
        }*/
        /*OutputStream os2 = null;
            try {
                    os2 = new FileOutputStream(System.getProperty("user.dir") + "/yyy/" + ConferenceID + "node" + ".txt");//创建一个文件路径
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
             
        BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(os2));
        for (Node n : graphModel.getDirectedGraph().getNodes()) {
            writer2.write(n.getAttribute(id).toString()+";");
            writer2.write(n.getAttribute(nodeColorCol).toString()+";");
            writer2.write(n.getAttribute(nodeSizeCol1).toString()+";");
            writer2.write(n.getAttribute(nodeSizeCol2).toString()+"\n");      
        }
        
        OutputStream os3 = null;
            try {
                    os3 = new FileOutputStream(System.getProperty("user.dir") + "/yyy/" + ConferenceID + "edge" + ".txt");//创建一个文件路径
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
        BufferedWriter writer3 = new BufferedWriter(new OutputStreamWriter(os3));
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            writer3.write(e.getAttribute(source).toString()+";");
            writer3.write(e.getAttribute(target).toString()+";");
            writer3.write(e.getAttribute(edgeStrokeCol).toString()+"\n");
        }*/
        
        
        Iterator<Node> nodeIter = graphModel.getGraphVisible().getNodes().iterator();
        while (nodeIter.hasNext()) {
            Node node = nodeIter.next();//node.setFixed(true);
            /*if(graph.getNodeCount()>=1290)
            {
            if ((int)(Double.parseDouble(node.getAttribute("size").toString()))<=30) node.setColor(new Color(0x9AFF9A));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=30&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=50) node.setColor(new Color(0xFFC125));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=50&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=75) node.setColor(new Color(0xFFA500));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=75&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=100) node.setColor(new Color(0xFF7F24));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=100&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=150) node.setColor(new Color(0xFF6347));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=100&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=125) node.setColor(new Color(0xFF6A6A));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=125&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=600) node.setColor(new Color(0xFF4500));
            else node.setColor(new Color(0xFF4500));}
            
            else if((graph.getNodeCount()<=800)&&(graph.getNodeCount()>=600))
                {
            if ((int)(Double.parseDouble(node.getAttribute("size").toString()))<=5) node.setColor(new Color(0x9AFF9A));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=5&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=10) node.setColor(new Color(0xFFC125));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=10&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=15) node.setColor(new Color(0xFFA500));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=15&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=20) node.setColor(new Color(0xFF7F24));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=20&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=25) node.setColor(new Color(0xFF6347));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=25&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=30) node.setColor(new Color(0xFF6A6A));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=30&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=40) node.setColor(new Color(0xFF4500));
            else node.setColor(new Color(0xFF4500));}
            
            else if(graph.getNodeCount()<=600)
                {
            if ((int)(Double.parseDouble(node.getAttribute("size").toString()))<=5) node.setColor(new Color(0x9AFF9A));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=5&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=8) node.setColor(new Color(0xFFC125));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=8&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=10) node.setColor(new Color(0xFFA500));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=10&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=15) node.setColor(new Color(0xFF7F24));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=15&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=20) node.setColor(new Color(0xFF6347));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=20&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=25) node.setColor(new Color(0xFF6A6A));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=25&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=50) node.setColor(new Color(0xFF4500));
            else node.setColor(new Color(0xFF4500));}
            
            else {
            if ((int)(Double.parseDouble(node.getAttribute("size").toString()))<=10) node.setColor(new Color(0x9AFF9A));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=10&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=20) node.setColor(new Color(0xFFC125));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=20&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=100) node.setColor(new Color(0xFFA500));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=100&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=125) node.setColor(new Color(0xFF7F24));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=125&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=150) node.setColor(new Color(0xFF6347));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=150&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=200) node.setColor(new Color(0xFF6A6A));
            else if ((int)(Double.parseDouble(node.getAttribute("size").toString()))>=200&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=250) node.setColor(new Color(0xFF4500));
            else node.setColor(new Color(0xFF4500));   
            }*/
            if(graphVisible.getNodeCount()>=585){
            if ((Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+((maxsize-minsize)/17))) node.setColor(new Color(0x425BFF));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/17)&&(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)/12)) node.setColor(new Color(0x00E1FF));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/12)&&(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)/8)) node.setColor(new Color(0x00D46C));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/8)&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)/5)) node.setColor(new Color(0xFFFD1A));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/5)&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)/3)) node.setColor(new Color(0xFFFD1A));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/3)&&(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)*3/4)) node.setColor(new Color(0xFF7800));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)*3/4)&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=maxsize) node.setColor(new Color(0xFF7800));
            //else node.setColor(new Color(0xFF4500));//
            }
            else{
            if ((Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+((maxsize-minsize)/11))) node.setColor(new Color(0x9AFF9A));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/11)&&(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)/7)) node.setColor(new Color(0xFFC125));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/7)&&(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)/5)) node.setColor(new Color(0xFFA500));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/5)&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)/3)) node.setColor(new Color(0xFF7F24));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/3)&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)/2)) node.setColor(new Color(0xFF6347));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)/2)&&(Double.parseDouble(node.getAttribute("size").toString()))<=(minsize+(maxsize-minsize)*2/3)) node.setColor(new Color(0xFF6A6A));
            else if ((Double.parseDouble(node.getAttribute("size").toString()))>=(minsize+(maxsize-minsize)*2/3)&&(int)(Double.parseDouble(node.getAttribute("size").toString()))<=maxsize) node.setColor(new Color(0xFF4500));
            }
            
            
        }
        System.out.println("setcolor" );
        if (true){
            Column strokeCol = graphModel.getEdgeTable().addColumn("stroke",String.class);
            String stroke, edgeColor;
            for (Edge e : graphModel.getDirectedGraph().getEdges()) {
                stroke = toHexFromColor(e.getSource().getColor());
                //nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                edgeColor = stroke;
                e.setAttribute(strokeCol, edgeColor);
            }
        }
        /*//Rank node color by year
        Column column = graphModel.getNodeTable().getColumn("year");
        Function func = appearanceModel.getNodeFunction(graph, column, RankingElementColorTransformer.class);
        //if (true) {
        if (func == null) {
            func = appearanceModel.getNodeFunction(graph, column, PartitionElementColorTransformer.class);
            System.out.println("Use PartitionElementColorTransformer");
            Partition partition = ((PartitionFunction) func).getPartition();
            partition.setColors(generateColors(partition.getValues()));
            appearanceController.transform(func);
        } else {
            System.out.println("Use RankingElementColorTransformer");
            RankingElementColorTransformer degreeTransformer = (RankingElementColorTransformer) func.getTransformer();
            degreeTransformer.setColors(new Color[]{new Color(0xFF0000), new Color(0xFCA2CD), new Color(0x0426E2)});
            degreeTransformer.setColorPositions(new float[]{0f, 0.5f, 1f});
            appearanceController.transform(func);
        }*/

        //Rank node size by nodeSizeCol

        nodeSizeCol = graphModel.getNodeTable().getColumn("size");
        Function sizeRanking = appearanceModel.getNodeFunction(graph, nodeSizeCol, RankingNodeSizeTransformer.class);

            RankingNodeSizeTransformer sizeTransformer = (RankingNodeSizeTransformer) sizeRanking.getTransformer();
            sizeTransformer.setMaxSize(300);
            sizeTransformer.setMinSize(15);
            appearanceController.transform(sizeRanking);
             

           
        
        if(graphVisible.getNodeCount()<=550||graphVisible.getEdgeCount()<=800){
            for (int loop=0;loop<2;loop++){
            YifanHuLayout layout=new YifanHuLayout(null,new StepDisplacement(20f));//20F
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            if(((graphVisible.getEdgeCount()-graphVisible.getNodeCount())<=300))
            {
            System.out.println("1!");
            layout.setOptimalDistance(50f);}//400f//50F
            else
            {System.out.println("2!");
            layout.setOptimalDistance(/*graphVisible.getEdgeCount()-graphVisible.getNodeCount())*2f*/400f);}
            layout.initAlgo();
            for (int i = 0; i < 3000 && layout.canAlgo(); i++) {
                layout.goAlgo();
            }//3000
            layout.endAlgo();
            }
        //ForceAtlas
        System.out.println("forceatlas!");
        ForceAtlasLayout faLayout = new ForceAtlasLayout(null);
        faLayout.setGraphModel(graphModel);
        faLayout.initAlgo();
        faLayout.resetPropertiesValues();
        //System.out.println(faLayout.getSpeed());
        faLayout.setRepulsionStrength(Math.min(6000.0, Math.max(graphVisible.getEdgeCount()*20, (double)graphVisible.getNodeCount()*20)));//6000.0//20
        faLayout.setMaxDisplacement(200.0);//200.0
        faLayout.setGravity(0.0);
        faLayout.setOutboundAttractionDistribution(Boolean.TRUE);
        faLayout.setSpeed(3.0);
        faLayout.setAdjustSizes(Boolean.TRUE);

        if(((graphVisible.getEdgeCount()-graphVisible.getNodeCount())<=300) || (maxsize<=10))
            {
        for (int i = 0; i < 500 && faLayout.canAlgo(); i++) {
            faLayout.goAlgo(); //System.out.print(i);//2000.0
        }
            }//2500}
        else{
            for (int i = 0; i < 2500 && faLayout.canAlgo(); i++) {
            faLayout.goAlgo(); //System.out.print(i);//2500.0
        }    
                }
        
        /*faLayout.setAdjustSizes(Boolean.TRUE);
        for (int i = 0; i < 100 && faLayout.canAlgo(); i++) {
            faLayout.goAlgo();
        }
        faLayout.endAlgo(); */
        
        NoverlapLayout layout2 = new NoverlapLayout(new NoverlapLayoutBuilder());
        layout2.setGraphModel(graphModel);
        layout2.resetPropertiesValues();
        layout2.setMargin(5.0);
        layout2.setRatio(1.2);
        layout2.setSpeed(3.0);
        layout2.initAlgo();
        for (int i = 0; i < Math.min(graphVisible.getNodeCount() / 4, 50) && layout2.canAlgo(); i++) 
        	layout2.goAlgo();
        layout2.endAlgo();
        }
        else {
        //Yifanhu
        System.out.println("forceatlas2!!");
        for (int loop=0;loop<2;loop++){
            YifanHuLayout layout=new YifanHuLayout(null,new StepDisplacement(20f));
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            layout.setOptimalDistance(400f);
            layout.initAlgo();
            for (int i = 0; i < 10000 && layout.canAlgo(); i++) {
                layout.goAlgo();
            }
            layout.endAlgo();
        }
       //ForceAtlas2
       ForceAtlas2 fa2Layout = new ForceAtlas2(new ForceAtlas2Builder());
       fa2Layout.setGraphModel(graphModel);
       fa2Layout.resetPropertiesValues();
       fa2Layout.setEdgeWeightInfluence(1.0);
       fa2Layout.setGravity(1.0);
       //if(nodecount<595) fa2Layout.setScalingRatio(1.0);
       if((graphVisible.getNodeCount()>=(graphVisible.getEdgeCount()))||(graphVisible.getEdgeCount()<=800))
       {fa2Layout.setScalingRatio(Math.min((0.003*(graphVisible.getNodeCount())+2.0)-(0.005*(graphVisible.getNodeCount()-graphVisible.getEdgeCount())),2.5));
       System.out.println((0.003*(graphVisible.getNodeCount())+2.0)-(0.005*(graphVisible.getNodeCount()-graphVisible.getEdgeCount())));}
       else 
       {fa2Layout.setScalingRatio(Math.max(0.003*graphVisible.getNodeCount()+2.0,5.0));}
       
       fa2Layout.setBarnesHutTheta(1.2);
       fa2Layout.setJitterTolerance(1.0);
       fa2Layout.setLinLogMode(Boolean.TRUE);
       fa2Layout.setAdjustSizes(Boolean.TRUE);
       fa2Layout.initAlgo();
     
        fa2Layout.setAdjustSizes(true);
       for (int i = 0; i < 10000 && fa2Layout.canAlgo(); i++) 
      	fa2Layout.goAlgo();
       fa2Layout.endAlgo();
       
       NoverlapLayout layout2 = new NoverlapLayout(new NoverlapLayoutBuilder());
        layout2.setGraphModel(graphModel);
        layout2.resetPropertiesValues();
        layout2.setMargin(5.0);
        layout2.setRatio(1.2);
        layout2.setSpeed(3.0);
        layout2.initAlgo();
        for (int i = 0; i < Math.min(graphVisible.getNodeCount(), 500) && layout2.canAlgo(); i++) 
        layout2.goAlgo();
        layout2.endAlgo();
        }
        float maxRadius=0, minRadius=1000;
        if (true){
            //Column colorCol = graphModel.getNodeTable().getColumn("fill");
            Column nodeRadiusCol = graphModel.getNodeTable().addColumn("Radius", Float.class);
            Column labelSizeCol = graphModel.getNodeTable().addColumn("labelsize", Float.class);
            float radius, nodeRadius;
            float labelsize,labelSize;
            
            for (Node n : graphModel.getDirectedGraph().getNodes()) {
                radius = n.size();
                if(radius>maxRadius) maxRadius = radius;
                if(radius<maxRadius) minRadius = radius;
                labelsize = radius * 3 / 4;
                //nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                nodeRadius = radius;
                labelSize = labelsize;
                n.setAttribute(nodeRadiusCol, nodeRadius);
                n.setAttribute(labelSizeCol, labelSize);
            }
        }
        Double a1 = (maxRadius - minRadius) / (maxsize1 - minsize1);
        Double b1 = minRadius - a1*minsize1;
        Double a2 = (maxRadius - minRadius) / (maxsize2 - minsize2);
        Double b2 = minRadius - a2*minsize2;
        /*if (true){
            //Column colorCol = graphModel.getNodeTable().getColumn("fill");
            Column nodeRadiusCol2 = graphModel.getNodeTable().addColumn("Radius2", Double.class);
            Column nodeRadiusCol3 = graphModel.getNodeTable().addColumn("Radius3", Double.class);
            Double radius2, nodeRadius2;
            Double radius3, nodeRadius3;
            for (Node n : graphModel.getDirectedGraph().getNodes()) {
                //radius2 = Double.parseDouble(n.getAttribute(nodeSizeCol1).toString());
                //radius3 = Double.parseDouble(n.getAttribute(nodeSizeCol2).toString());
                //nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                //nodeRadius2 = radius2 * a1 + b1;
                //nodeRadius3 = radius3 * a2 + b2;
                //n.setAttribute(nodeRadiusCol2, nodeRadius2);
                //n.setAttribute(nodeRadiusCol3, nodeRadius3);
            }
        }*/
        //Filter
        /*InDegreeRangeFilter inDegreeFilter = new InDegreeRangeFilter();
        inDegreeFilter.init(graph);
        inDegreeFilter.setRange(new Range(1, Integer.MAX_VALUE));     //Remove nodes with inDegree = 0
        Query query = filterController.createQuery(inDegreeFilter);
        GraphView view = filterController.filter(query);
        graphModel.setVisibleView(view);*/
        
        //Preview
      //  PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        //model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(new Color(186, 172, 212)));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.SHOW_EDGES, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.FALSE);        
        model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float (20.0));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, new java.awt.Font("Dialog", 0, 1));
        model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
        model.getProperties().putValue(PreviewProperty.NODE_BORDER_WIDTH,0);
        model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);


        //previewController.refreshPreview();

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        File svgFile = new File(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "3.svg");
        File pngFile = new File(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "3.png");
        //File gephiFile = new File(System.getProperty("user.dir") + "/yyy1/" + ConferenceID + ".gephi");
        File gml2File = new File(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "3_.gml");
        

        ec.exportFile(svgFile);
        ec.exportFile(pngFile);
        ec.exportFile(gml2File);
        //pc.saveProject(pc.getCurrentProject(), gephiFile).run();
        
        //GraphView view2 = graphModel.getVisibleView();
        

        
		InputStream is = null;
		OutputStream os = null;

		is = new FileInputStream(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "3.svg");
		os = new FileOutputStream(System.getProperty("user.dir") + "/yyy6/" + ConferenceID + "3.php");

        String line = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        for (int i = 0; i < 9; ++i) { //skip useless codes
        	reader.readLine();
        }
        line = reader.readLine();
        while (line != null) {
            writer.write(line + '\n');
            line = reader.readLine();
        }
        reader.close();
        writer.close();
        is.close();
        os.close();
        isStop=0;
        //svgFile.delete();
        //gmlFile.delete();
	}
       
       
        //New Processing target, get the PApplet
    /*    G2DTarget target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
        PreviewSketch previewSketch = new PreviewSketch(target);
        previewController.refreshPreview();
        previewSketch.resetZoom();
        
        //Export
//        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
//        try {
//            ec.exportFile(new File("wirelessnetwork.png"));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            return;
//        }
//        
//		InputStream is = null;
//		OutputStream os = null;
//		try {
//			is = new FileInputStream("wirelessnetwork.svg");
//			os = new FileOutputStream("out.php");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//        String line = null; // 用来保存每行读取的内容
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
//        for (int i = 0; i < 9; ++i) { //skip useless codes
//        	reader.readLine();
//        }
//        line = reader.readLine();
//        while (line != null) { // 如果 line 为空说明读完了
//            writer.write(line + '\n');
//            line = reader.readLine(); // 读取下一行
//        }
//        reader.close();
//        writer.close();
//        is.close();
        
        //Add the applet to a JFrame and display
        JFrame frame = new JFrame("Test Preview");
        frame.setLayout(new BorderLayout());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(previewSketch, BorderLayout.CENTER);

        frame.setSize(1024, 768);
        frame.setVisible(true);
       
        }       */
        
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {        
		OutputStream os = null;

		os = new FileOutputStream("logs.txt");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
		Class.forName("com.mysql.jdbc.Driver");
                // or:
                // com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
                // or：
                // new com.mysql.jdbc.Driver();
		
        System.out.println("成功加载MySQL驱动！");
            
        String url="jdbc:mysql://rm-uf6g9z279sr18j926do.mysql.rds.aliyuncs.com:3306/mag-new-160205";    //JDBC的URL URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值   
		try {
			conn = DriverManager.getConnection(url, "groupleader","Onlyleaders0");     // 一个Connection代表一个数据库连接
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
        try {
			stmt = conn.createStatement();
			stmt2 = conn.createStatement();     // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
                        stmt3 = conn.createStatement();
        } catch (SQLException e1) {
			e1.printStackTrace();
		}
        System.out.println("成功连接到数据库！");
        
        String sql = "SELECT * FROM ConferenceSeries";
        ResultSet rs2 = stmt2.executeQuery(sql);            // executeQuery会返回结果的集合，否则返回空值
        String ConferenceID = "";
        String ConferenceName = "";
        //int totalAmountOfPapers = 0;
        int cnt = 0;
        while (rs2.next()) {
			try {
				ConferenceID = rs2.getString("ConferenceSeriesID");
				ConferenceName = rs2.getString("FullName");
				//totalAmountOfPapers = rs2.getInt("TotalAmountOfPapers");
				System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				System.out.println(ConferenceName);
                                System.out.println(ConferenceID);
				//System.out.println(totalAmountOfPapers);
	        	writer.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
	        	writer.newLine();
	        	writer.write(ConferenceName);
	        	writer.newLine();
	        	generateSvg(ConferenceID,0);
			} catch (Exception e1) {
				e1.printStackTrace();
				try {
					writer.write(e1.getMessage());
					writer.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} finally {
				++cnt;
				System.out.println("Having processed " + cnt + " fields");
			}
        }
        rs2.close();
//        String sql = "SELECT * FROM ConferenceSeries LIMIT 965,36";
//        ResultSet rs2 = stmt2.executeQuery(sql);            // executeQuery会返回结果的集合，否则返回空值
//        String ConferenceID = "";
//        String ConferenceName = "";
//        //int totalAmountOfPapers = 0;
//        int cnt = 0;
//        while (rs2.next()) {
//			try {
//				ConferenceID = rs2.getString("ConferenceSeriesID");
//				ConferenceName = rs2.getString("FullName");
//				//totalAmountOfPapers = rs2.getInt("TotalAmountOfPapers");
//				System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//				System.out.println(ConferenceName);
//                                System.out.println(ConferenceID);
//				//System.out.println(totalAmountOfPapers);
//	        	writer.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//	        	writer.newLine();
//	        	writer.write(ConferenceName);
//	        	writer.newLine();
//	        	//writer.write(totalAmountOfPapers);
//	        	//writer.newLine();
//                                Service service = new Service();
//                                service.ID = ConferenceID;
//                                System.out.println("测试开始");
//                                isStop=1;
//                                service.start();
//                                int timepast = 0;
//                                while (isStop==1 && timepast <= 480)
//                                {
//                                timepast++;
//                                if(timepast == 100 || timepast == 200 || timepast == 300 || timepast == 50 || timepast == 150 || timepast == 250 || timepast == 350 || timepast == 400)
//                                {sql = "SELECT * FROM ConferenceSeries LIMIT 1";
//                                ResultSet rs3 = stmt3.executeQuery(sql); }
//                                Thread.sleep(1*1000);
//                                }
//                                if(isStop==1){
//                                    System.out.println("Stopped!!!");
//                                      
//
//                                    generateSvg(ConferenceID,0);
//                                }
//                                else{
//                                    System.out.println("Nonstopped!!!");
//                                }
//
//                                service.stop();
//                                
//			} catch (Exception e1) {
//				e1.printStackTrace();
//				try {
//					writer.write(e1.getMessage());
//					writer.newLine();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			} finally {
//				++cnt;
//				System.out.println("Having processed " + cnt + "conferences");
//			}
//        }
//        rs2.close();
        
        //generateSvg("42F4F2CC",0); 
        //generateSvg("442BD7CD",0);
        //generateSvg("42F4F2CC",1);
        //generateSvg("44B13001",1);
        //generateSvg("46A355BA",1);
        /*generateSvg("469BDC4B",1);
        
        generateSvg("43319DD4",1);
        generateSvg("45FB3662",1);
        generateSvg("43ABF249",1);
        generateSvg("436976F3",1);*/
        //generateSvg("43F6459B",1);
        //generateSvg("460A7036",1);
        //generateSvg("45610CDA",1);
        //generateSvg("43ABF249",1);
        //generateSvg("468A7487",1);
        //generateSvg("465F7C62",1);
        //generateSvg("46A05BB0",1);
        //generateSvg("47C39427",1);
        //generateSvg("43226B44",1);
        //generateSvg("436EE6FE",1);
        //generateSvg("44616FF1",1);
        //generateSvg("4499A295",1);
        //generateSvg("43F6459B",1);
        
        
	stmt.close();
        conn.close();
        stmt2.close();
        stmt3.close();
        writer.close();
        os.close();
	}
        
}


//e.toString()：  获得异常种类和错误信息
//e.getMessage(): 获得错误信息
//e.printStackTrace()：在控制台打印出异常种类，错误信息和出错位置等
/**
 * public class Main {

    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    public static String TEST_XML_STRING =
        "<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";

    public static void main(String[] args) {
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            System.out.println(jsonPrettyPrintString);
        } catch (JSONException je) {
            System.out.println(je.toString());
        }
    }
}
 */   

