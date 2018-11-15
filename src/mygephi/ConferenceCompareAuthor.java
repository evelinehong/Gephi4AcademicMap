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
import java.awt.Color;


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
import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.layout.plugin.rotate.Rotate;
import org.gephi.layout.plugin.rotate.RotateLayout;

public class ConferenceCompareAuthor {
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
        
  
	
	public static void generateGml(String ConferenceID1, String ConferenceID2) {
		try{
                    String ConferenceID3 = ConferenceID1;
                    String ConferenceID4 = ConferenceID2;
            HashMap<String , Color> hashMap = new HashMap<String , Color>();
            String sql = "SELECT * FROM ConferenceSeries WHERE ConferenceSeriesID='" + ConferenceID1 + "'";
            ResultSet rs = stmt.executeQuery(sql);

            sql =        
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
                          "ConferenceSeriesIDMappedToVenueName = '"+ConferenceID1+"' \n" +
                          ") AS tb2 ON PaperAuthorAffiliations.PaperID = tb2.PaperID \n" +
                          "GROUP BY AuthorID ORDER BY counts DESC LIMIT 1000\n" ;
              
            rs = stmt.executeQuery(sql);
            System.out.println("Retrieved authors id successfully.");
            

                    HashMap<String , Integer> hashMap1 = new HashMap<String , Integer>();
                    String id1="";
                    int count1 = 0;
                    int totalcount1 = 0;
                    int mincount1 = 1000, maxcount1 = 0;
                    while (rs.next()) {
                    id1 = rs.getString("AuthorID");
                    count1 = Integer.parseInt(rs.getString("counts"));
                    if(count1>maxcount1) maxcount1 = count1;
                    else mincount1 = count1;
                    hashMap1.put(id1, count1);
                    totalcount1 = totalcount1 + count1;
                  
            }
                    Double a1 = 100.0 / (maxcount1 - mincount1);
                    Double b1 = 150.0 - mincount1 * a1;
            rs.close(); 
            //2
            String sql2 = "SELECT * FROM ConferenceSeries WHERE ConferenceSeriesID='" + ConferenceID2 + "'";
            ResultSet rs2 = stmt.executeQuery(sql2);

            sql2 =        
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
                          "ConferenceSeriesIDMappedToVenueName = '"+ConferenceID2+"' \n" +
                          ") AS tb2 ON PaperAuthorAffiliations.PaperID = tb2.PaperID \n" +
                          "GROUP BY AuthorID ORDER BY counts DESC LIMIT 1000\n" ;
              
            rs2 = stmt.executeQuery(sql2);
            System.out.println("Retrieved authors id successfully.");
                    HashMap<String , Integer> hashMap2 = new HashMap<String , Integer>();
                    String id2="";
                    int count2 = 0;
                    int totalcount2 = 0;
                    int mincount2 = 1000, maxcount2 = 0;
                    while (rs2.next()) {
                    id2 = rs2.getString("AuthorID");
                    count2 = Integer.parseInt(rs2.getString("counts"));
                    if(count2>maxcount2) maxcount2 = count2;
                    else mincount2 = count2;
                    hashMap2.put(id2, count2);
                    totalcount2 = totalcount2 + count2;
                  
            }
                    Double a2 = 100.0 / (maxcount2 - mincount2);
                    Double b2 = 150.0 - mincount2 * a2;
            rs2.close(); 
            //3
            /*String sql3 = "SELECT * FROM ConferenceSeries WHERE ConferenceSeriesID='" + ConferenceID3 + "'";
            ResultSet rs3 = stmt.executeQuery(sql3);

            sql3 =        
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
                          "ConferenceSeriesIDMappedToVenueName = '"+ConferenceID3+"' \n" +
                          ") AS tb2 ON PaperAuthorAffiliations.PaperID = tb2.PaperID \n" +
                          "GROUP BY AuthorID ORDER BY counts DESC LIMIT 500\n" ;
              
            rs3 = stmt.executeQuery(sql3);
            System.out.println("Retrieved authors id successfully.");

                    String id3="";
                    int count3 = 0;
                    int mincount3 = 1000, maxcount3 = 0;
                    while (rs3.next()) {
                    id3 = rs3.getString("AuthorID");
                    count3 = Integer.parseInt(rs3.getString("counts"));
                    if(count3>maxcount3) count3 = maxcount3;
                    else count3 = mincount3;
                    hashMap.put(id3, count3);
                  
            }
                    Double a3 = 66.0 / (maxcount3 - mincount3);
                    Double b3 = 139.0 - mincount3 * a3;
            rs3.close(); 
*/
            /*Iterator iter1 = hashMap1.entrySet().iterator();
            while(iter1.hasNext()){
                Color color = new Color(0,0,0);
                Map.Entry entry1 = (Map.Entry) iter1.next();
                if(!hashMap.containsKey(entry1.getKey()))
                {color = new Color((int)(a1*(Integer.parseInt(entry1.getValue().toString()))+b1),0,0);
                hashMap.put(entry1.getKey().toString(), color);
                }
                
            }
            Iterator iter2 = hashMap2.entrySet().iterator();
            while(iter2.hasNext()){
                Color color = new Color(0,0,0);
                Map.Entry entry2 = (Map.Entry) iter2.next();
                if(!hashMap.containsKey(entry2.getKey()))
                {color = new Color(0,(int)(a2*(Integer.parseInt(entry2.getValue().toString()))+b2),0);
                hashMap.put(entry2.getKey().toString(), color);
                }
                else
                { 
                color = hashMap.get(entry2.getKey());
                {color = new Color(color.getRed(),(int)(a2*(Integer.parseInt(entry2.getValue().toString()))+b2),0);
                //color = new Color((Integer.parseInt(entry3.getValue().toString().substring(17, 19))),(int)(a2*(Integer.parseInt(entry2.getValue().toString()))+b2),0);
                hashMap.remove(entry2.getKey());
                hashMap.put(entry2.getKey().toString(), color);
                }
                }*/
             HashMap<String , Integer> hashMap4 = new HashMap<String , Integer>();
             HashMap<String , String> hashMap5 = new HashMap<String , String>();
            if(totalcount2 > totalcount1) {hashMap4 = hashMap1; hashMap1 = hashMap2; hashMap2 = hashMap4;
            ConferenceID3 = ConferenceID1; ConferenceID1 = ConferenceID2; ConferenceID2 = ConferenceID3;}
               Iterator iter1 = hashMap1.entrySet().iterator();
            while(iter1.hasNext()){
                Color color = new Color(0,0,0);
                Map.Entry entry1 = (Map.Entry) iter1.next();
                if(!hashMap.containsKey(entry1.getKey()))
                {color = new Color(201,23,30);
                hashMap.put(entry1.getKey().toString(), color);
                hashMap5.put(entry1.getKey().toString(), ConferenceID1);
                }
                
            }
            Iterator iter2 = hashMap2.entrySet().iterator();
            while(iter2.hasNext()){
                Color color = new Color(0,0,0);
                Map.Entry entry2 = (Map.Entry) iter2.next();
                if(!hashMap.containsKey(entry2.getKey()))
                {color = new Color(34,125,81);
                hashMap.put(entry2.getKey().toString(), color);
                hashMap5.put(entry2.getKey().toString(), ConferenceID2);
                }
                else
                { 
                color = hashMap.get(entry2.getKey());
                {color = new Color(248,181,0);
                //color = new Color((Integer.parseInt(entry3.getValue().toString().substring(17, 19))),(int)(a2*(Integer.parseInt(entry2.getValue().toString()))+b2),0);
                hashMap.remove(entry2.getKey());
                hashMap.put(entry2.getKey().toString(), color);
                hashMap5.put(entry2.getKey().toString(), ConferenceID1+"_"+ConferenceID2);
                }
                } 
                
            }
            OutputStream os = null;
            try {
                    os = new FileOutputStream(System.getProperty("user.dir") + "/compare4/" + ConferenceID3 + " " +  ConferenceID4 + ".gml");//创建一个文件路径
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
            HashMap<String , Color> hashMap3 = new HashMap<String , Color>();
            hashMap3 = hashMap;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            
            writer.write("graph [\n");
            writer.write("directed 1\n");

            HashSet<String> hashSet = new HashSet<String>();
            
            Iterator iter = hashMap.entrySet().iterator();//map.entrySet()是把HashMap类型的数据转换成集合类型 map.entrySet().iterator()是去获得这个集合的迭代器，保存在iter里面。
            
            
            int totalprocess=0;
            double numberofpapers;
            double size=0.0,size1=0.0,size2=0.0;
            int cnt = 0;
            while (iter.hasNext()) {//若仍有元素可以迭代就返回true
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();//不确定传进来的是什么类就用这个笼统的
                Object val = entry.getValue();
                String Conference = hashMap5.get(key);
                
                if (true) {
                 cnt++ ;
                System.out.println(key+"NODE"  + ": " + ++totalprocess);
                
                sql = "SELECT * FROM\n" +
                      "(SELECT AuthorName FROM Authors\n" +
                      "INNER JOIN\n" +
                      "(SELECT AuthorID FROM AuthorInfo WHERE AuthorID='"+key+"')AS TB1\n" +
                      "ON Authors.AuthorID=TB1.AuthorID)AS TB2";
                
                rs = stmt.executeQuery(sql);
                while(rs.next()) {
                 if ((!hashSet.contains(key.toString()))){
                        size=0.0;size1=0.0;size2=1.0;
                        writer.write("node [\n");
                        hashSet.add(key.toString());
                        writer.write("id \"" + key.toString() + "\"\n");
                        writer.write("label \"" + capitalize(rs.getString("AuthorName")) + "\"\n"); 
                        //writer.write("ConferenceID \"" + val.toString() + "\"\n");
                 sql = "SELECT * FROM\n" +
                      "((SELECT PaperID, AuthorSequenceNumber\n" +
                      "FROM PaperAuthorAffiliations WHERE AuthorID = '"+key+"')AS TB1\n" +
                      "INNER JOIN \n" +
                      "(SELECT PaperID,CitationCount\n" +
                      "FROM Papers\n" +
                      "WHERE ConferenceSeriesIDMappedToVenueName = '"+ConferenceID1+ "'OR ConferenceSeriesIDMappedToVenueName = '"+ConferenceID2+ "')AS TB3\n" +
                      "ON TB1.PaperID = TB3.PaperID)";
                          
                 rs = stmt.executeQuery(sql);
                 while(rs.next()) {
                     String citation = rs.getString("CitationCount");
                     
                     //size=size+1+Double.valueOf(citation.toString()).doubleValue()/(Double.valueOf(rs.getString("AuthorSequenceNumber")).doubleValue())/100;
                     //size1=size1+1;
                     size=size+(Double.valueOf(citation.toString()).doubleValue());
                     //System.out.println(citation+ " " + AuthorSequenceNumber + " " + citationcount + " " + size +" "+size1+" "+ size2+" ");
                     
                 }      
                       // writer.write("size "+ ""+ size + "\n");
                       // writer.write("size1 "+ ""+ size1 + "\n");
                        writer.write("conference " +"" + Conference + "\n");
                        writer.write("fill " + "" + "#" + toHexFromColor(hashMap.get(key)) + "\n");
                        writer.write("size " + ""+ size + "\n]\n");
                        
                }
                }
              
 
                }
    
            }
            writer.write("node [\n");
            writer.write("id \"" + "00000000" + "\"\n");
            writer.write("label \"" + "   "+ "\"\n");  
            writer.write("fill " + "" + "#" + "FFFFFF" + "\n");
            writer.write("conference "+ "" + ConferenceID1 + "\n");
            writer.write("size " + "" + "1000" + "\n]\n");
            writer.write("node [\n");
            writer.write("id \"" + "00000001" + "\"\n");
            writer.write("label \"" + "   "+ "\"\n");  
            writer.write("fill " + "" + "#" + "FFFFFF" + "\n");
            writer.write("conference "+ "" + ConferenceID2 + "\n");
            writer.write("size " + "" + "1000" + "\n]\n");
            
            Iterator<String> iterator= hashSet.iterator();
            while (iterator.hasNext())
            {  
                String key1 = iterator.next();
               sql = "(SELECT DISTINCT AuthorID, COUNT( AuthorID) AS counts FROM\n "+
                         "(SELECT AuthorID, PaperAuthorAffiliations.PaperID FROM PaperAuthorAffiliations\n"+
                         "INNER JOIN\n"+
                         "(SELECT PaperAuthorAffiliations.PaperID FROM PaperAuthorAffiliations WHERE AuthorID='"+key1+"')AS tb1\n"+
                         "ON PaperAuthorAffiliations.PaperID = tb1.PaperID)AS tb2\n" +
                         "INNER JOIN\n"+
                         "(SELECT Papers.PaperID FROM Papers WHERE ConferenceSeriesIDMappedToVenueName = '"+ConferenceID1+ "'OR ConferenceSeriesIDMappedToVenueName = '"+ConferenceID2+"')AS tb3\n"+
                         "ON tb2.PaperID = tb3.PaperID\n" +
                         "GROUP BY AuthorID HAVING counts>1 \n" +
                         "ORDER BY counts DESC )";
                   rs = stmt.executeQuery(sql);
                   while(rs.next()){
                       String tmp = rs.getString("AuthorID");
                       int val1=(rs.getInt("counts"));
            		if (hashSet.contains(tmp) && !tmp.equals(key1.toString()) && val1>=0.0) {
                            
                                System.out.println(key1+"EDGE: "+ ++totalprocess);
            			writer.write("edge [\n");
            			writer.write("source \"" + key1.toString() + "\"\n");
            			writer.write("target \"" + tmp + "\"\n");
                                writer.write("value " + val1 + "\n]\n");

            		}
                        
                   }
                    rs.close();
                if((!hashMap2.containsKey(key1)) && hashSet.contains(key1)){
                writer.write("edge [\n");
            	writer.write("source \"" + key1.toString() + "\"\n");
            	writer.write("target \"" + "00000000" + "\"\n");
                writer.write("stroke " + "" + "#" + "FFFFFF" + "\n");
                writer.write("value "+ "" + "0.133" + "\n]\n");    
                }
                else if ((!hashMap1.containsKey(key1)) && hashSet.contains(key1)){
                writer.write("edge [\n");
            	writer.write("source \"" + key1.toString() + "\"\n");
            	writer.write("target \"" + "00000001" + "\"\n");
                writer.write("stroke " + "" + "#" + "FFFFFF" + "\n");
                writer.write("value " + ""+"0.133" + "\n]\n");     
                }
                else if (hashMap1.get(key1)>=hashMap2.get(key1)){
                   writer.write("edge [\n");
            	writer.write("source \"" + key1.toString() + "\"\n");
            	writer.write("target \"" + "00000000" + "\"\n");
                writer.write("stroke " + "" + "#" + "FFFFFF" + "\n");
                writer.write("value "+ "" + "0.133" + "\n]\n");    
                }
                else if (hashMap1.get(key1)<=hashMap2.get(key1)){
                   writer.write("edge [\n");
            	writer.write("source \"" + key1.toString() + "\"\n");
            	writer.write("target \"" + "00000001" + "\"\n");
                writer.write("stroke " + "" + "#" + "FFFFFF" + "\n");
                writer.write("value "+ "" + "0.133" + "\n]\n");} 
            }
            
            /*Iterator iter3 = hashMap3.entrySet().iterator();//map.entrySet()是把HashMap类型的数据转换成集合类型 map.entrySet().iterator()是去获得这个集合的迭代器，保存在iter里面。
            while (iter3.hasNext()) {//若仍有元素可以迭代就返回true
                Map.Entry entry = (Map.Entry) iter3.next();
                Object key = entry.getKey();//不确定传进来的是什么类就用这个笼统的
                Object val = entry.getValue();
                if((hashMap3.get(key).getRed())>=(hashMap3.get(key).getGreen())&&hashSet.contains(key)){
                writer.write("edge [\n");
            	writer.write("source \"" + key.toString() + "\"\n");
            	writer.write("target \"" + "00000000" + "\"\n");
                writer.write("value " + 1 + "\n]\n");    
                }
                else{
                writer.write("edge [\n");
            	writer.write("source \"" + key.toString() + "\"\n");
            	writer.write("target \"" + "00000001" + "\"\n");
                writer.write("value " + 1 + "\n]\n");     
                }
            }*/
        
            writer.write("]");
            writer.close();
            os.close();
            
            System.out.println("Content written to " + "compare9" + ".gml");
            rs.close();

            
       } catch(Exception e) {
    	   e.printStackTrace();

       }
          
        
	}
	
	public static void generateSvg(String ConferenceID1, String ConferenceID2) throws IOException, SQLException {
		/*File file = new File(System.getProperty("user.dir") + "/papermaps/" + fieldOfStudyID + ".php");
		if (file.exists()) {
			System.out.println(fieldOfStudyID + " already exists");
			return;
		}*/
                String ConferenceID3 = ConferenceID1;
                String ConferenceID4 = ConferenceID2;
                isStop = 1;
		generateGml(ConferenceID1, ConferenceID2);
		
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
        File gmlFile = new File(System.getProperty("user.dir") + "/compare4/" + ConferenceID3 +  " " + ConferenceID4  + ".gml");
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
        degreeFilter.setRange(new Range(0, Integer.MAX_VALUE));     //Remove nodes with degree = 0
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


        Column indgreeCol = graphModel.getNodeTable().addColumn("InDgree", Integer.class);
        for (Node n : graphModel.getDirectedGraph().getNodes()) {
            n.setAttribute(indgreeCol, graphModel.getDirectedGraph().getInDegree(n));

        }
        //Add node size column
        Column ConferenceCol = graphModel.getNodeTable().getColumn("conference");
        Column nodeSizeCol = graphModel.getNodeTable().getColumn("size");
        //Column ConferenceCol = graphModel.getNodeTable().getColumn("ConferenceID");
        Column colorCol= graphModel.getNodeTable().getColumn("fill");
        Column id = graphModel.getNodeTable().getColumn("id");
        Double numberofpapers,numberofcitation,nodeIndgree,nodeSize,size;
        String ConferenceID, confID, Color;
        String color;
        Color color1;
        Double maxsize=0.0, minsize=1000.0;
        Double totalsize1 = 0.0, totalsize2 = 0.0;
        String id2;
        for (Node n : graphModel.getDirectedGraph().getNodes()) { 
            id2 = (n.getAttribute(id).toString());
            size = Double.parseDouble(n.getAttribute(nodeSizeCol).toString());
            //color = n.getAttribute(colorCol).toString();
            //Color = color;
            if(size>maxsize) maxsize = size;
            if(size<minsize) minsize = size;
            nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
            nodeSize = size;
            n.setAttribute(nodeSizeCol, nodeSize);
            
            confID = (n.getAttribute(ConferenceCol).toString());
            if(confID.equals(ConferenceID1)||confID.equals(ConferenceID1+"_"+ConferenceID2)||confID.equals(ConferenceID2+"_"+ConferenceID1)) totalsize1 ++;
            else if(confID.equals(ConferenceID2)||confID.equals(ConferenceID1+"_"+ConferenceID2)||confID.equals(ConferenceID2+"_"+ConferenceID1))totalsize2++;
            System.out.println(confID);
            ConferenceID = confID;
            //System.out.println(ConferenceID);
            n.setAttribute(ConferenceCol, confID);
            
        }

        System.out.println("maxsize:" + maxsize);
        System.out.println("minsize:" + minsize);

        Column source = graphModel.getEdgeTable().getColumn("source");
        Column target = graphModel.getEdgeTable().getColumn("target");
        Column weightCol = graphModel.getEdgeTable().addColumn("weight2",Double.class);
        Double weight, edgeweight;
        String source1;
        //Set edge weight
        /*Double sourceYear,targetYear;*/
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            //source1 = (e.getSource().toString());
            e.setWeight((Math.log10(e.getWeight())+1.0));
        }
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            source1 = (e.getSource().toString());
            if((e.getSource().getAttribute(id).equals("00000000")) || (e.getTarget().getAttribute(id).equals("00000000")) ||(e.getSource().getAttribute(id).equals("00000001")) || (e.getTarget().getAttribute(id).equals("00000001") )){
                e.setColor(new Color(255,255,255));
            }
            if(e.getSource().getAttribute(id).equals("00000000")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getSource().getAttribute(id).equals("00000001")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getTarget().getAttribute(id).equals("00000001")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getTarget().getAttribute(id).equals("00000000")){
                    e.setColor(new Color(255,255,255));
            }
        }

        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);

        Iterator<Node> nodeIter = graphModel.getGraphVisible().getNodes().iterator();
        while (nodeIter.hasNext()) {
            Node node = nodeIter.next();
            //System.out.println(node.getAttribute(ConferenceCol));
            if(node.getAttribute(ConferenceCol).toString().equals(ConferenceID1)&&totalsize1>=totalsize2){
            //System.out.println("3");
            node.setColor(new Color(201,23,30));}
            else if(node.getAttribute(ConferenceCol).toString().equals(ConferenceID1)&&totalsize1<totalsize2){
            //System.out.println("3");
            node.setColor(new Color(34,125,81));}
            else if(node.getAttribute(ConferenceCol).toString().equals(ConferenceID2)&&totalsize1<totalsize2){
            //System.out.println("3");
            node.setColor(new Color(201,23,30));}
            else if(node.getAttribute(ConferenceCol).toString().equals(ConferenceID2)&&totalsize1>=totalsize2){
            //System.out.println("3");
            node.setColor(new Color(34,125,81));}
            else  node.setColor(new Color(248,181,0));
                    
            //else node.setColor(new Color(0xFF4500));//
            }
            /*if (node.getAttribute(ConferenceCol).toString().equals("42F4F2CC")){
            //System.out.println("4");
            node.setColor(new Color(0x8B1A1A));
            
            }
            /*else if (node.getAttribute(ConferenceCol).toString().equals("43226B44")){
            //System.out.println("2");
            node.setColor(new Color(0x8B5A00));
            }*/
            /*else if (node.getAttribute(ConferenceCol).toString().equals("44B13001")){
            //System.out.println("5");
            node.setColor(new Color(0x388E3E));
            }
            else if (node.getAttribute(ConferenceCol).toString().equals("442BD7CD")){
            //System.out.println("1");
            node.setColor(new Color(0x27408B));
            }
            
        }
        System.out.println("setcolor" );*/
        /*if (true){
            Column strokeCol = graphModel.getEdgeTable().addColumn("stroke",String.class);
            String stroke, edgeColor;
            for (Edge e : graphModel.getDirectedGraph().getEdges()) {
                stroke = toHexFromColor(e.getSource().getColor());
                //nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                edgeColor = stroke;
                e.setAttribute(strokeCol, edgeColor);
            }
        }
        if (true){
            Column strokeCol = graphModel.getNodeTable().addColumn("stroke",String.class);
            String stroke, nodeColor;
            for (Node n : graphModel.getDirectedGraph().getNodes()) {
                stroke = toHexFromColor(n.getColor());
                //nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                nodeColor = stroke;
                n.setAttribute(strokeCol, nodeColor);
            }
        }*/


        //Rank node size by nodeSizeCol
        nodeSizeCol = graphModel.getNodeTable().getColumn("Size");
        Function sizeRanking = appearanceModel.getNodeFunction(graph, nodeSizeCol, RankingNodeSizeTransformer.class);
 
            RankingNodeSizeTransformer sizeTransformer = (RankingNodeSizeTransformer) sizeRanking.getTransformer();
            sizeTransformer.setMaxSize(1000);
            sizeTransformer.setMinSize(100);
            appearanceController.transform(sizeRanking);
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            //source1 = (e.getSource().toString());
            e.setWeight((Math.log10(e.getWeight())+1.0));
        }
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            source1 = (e.getSource().toString());
            if((e.getSource().getAttribute(id).equals("00000000")) || (e.getTarget().getAttribute(id).equals("00000000")) ||(e.getSource().getAttribute(id).equals("00000001")) || (e.getTarget().getAttribute(id).equals("00000001") )){
                e.setColor(new Color(255,255,255));
            }
            if(e.getSource().getAttribute(id).equals("00000000")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getSource().getAttribute(id).equals("00000001")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getTarget().getAttribute(id).equals("00000001")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getTarget().getAttribute(id).equals("00000000")){
                    e.setColor(new Color(255,255,255));
            }
        }

          

        //Yifanhu

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
       fa2Layout.setScalingRatio(Math.max(0.003*graphVisible.getNodeCount()+1.0,9.0));
       
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
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            //source1 = (e.getSource().toString());
            e.setWeight((Math.log10(e.getWeight())+1.0));
        }
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            source1 = (e.getSource().toString());
            if((e.getSource().getAttribute(id).equals("00000000")) || (e.getTarget().getAttribute(id).equals("00000000")) ||(e.getSource().getAttribute(id).equals("00000001")) || (e.getTarget().getAttribute(id).equals("00000001") )){
                e.setColor(new Color(255,255,255));
            }
            if(e.getSource().getAttribute(id).equals("00000000")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getSource().getAttribute(id).equals("00000001")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getTarget().getAttribute(id).equals("00000001")){
                    e.setColor(new Color(255,255,255));
            }
            if(e.getTarget().getAttribute(id).equals("00000000")){
                    e.setColor(new Color(255,255,255));
            }
        }

       /*     Double angle = 0.0;
            for (Node n : graphModel.getDirectedGraph().getNodes()) { 
            id2 = (n.getAttribute(id).toString());
            
            if(id2.equals("00000000"))
            {float tan = (n.y())/(n.x());
            angle = ((Math.atan(tan))*180)/Math.PI;
            System.out.println(angle);
            }
        }
            RotateLayout rotate = new RotateLayout(null,angle);
            rotate.setAngle(angle);
            rotate.initAlgo();
             for (int i = 0; i < 100 && rotate.canAlgo(); i++) 
            rotate.goAlgo();
            rotate.endAlgo();*/

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
        model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 20);
        model.getProperties().putValue(PreviewProperty.NODE_BORDER_WIDTH,0);
                        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            //source1 = (e.getSource().toString());
            e.setWeight((Math.log10(e.getWeight())+1.0));
        }
        for (Edge e : graphModel.getUndirectedGraph().getEdges()) {
            source1 = (e.getSource().toString());
            if((e.getSource().getAttribute(id).equals("00000000")) || (e.getTarget().getAttribute(id).equals("00000000")) ||(e.getSource().getAttribute(id).equals("00000001")) || (e.getTarget().getAttribute(id).equals("00000001") )){
                e.setColor(new Color(255,255,255));
            }
            if(e.getSource().getAttribute(id).equals("00000000")){
                    e.setColor(new Color(255,255,255));
                    //System.out.println("1");
            }
            if(e.getSource().getAttribute(id).equals("00000001")){
                    e.setColor(new Color(255,255,255));
                    //System.out.println("2");
            }
            if(e.getTarget().getAttribute(id).equals("00000001")){
                    e.setColor(new Color(255,255,255));
                   // System.out.println("3");
            }
            if(e.getTarget().getAttribute(id).equals("00000000")){
                    e.setColor(new Color(255,255,255));
                    //System.out.println("4");
            }
        }

            

        //previewController.refreshPreview();

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        File svgFile = new File(System.getProperty("user.dir") + "/compare4/" + ConferenceID1 + " " + ConferenceID2 + ".svg");
        File pngFile = new File(System.getProperty("user.dir") + "/compare4/" + ConferenceID1 + " " + ConferenceID2 + ".png");
        //File gephiFile = new File(System.getProperty("user.dir") + "/yyy1/" + ConferenceID + ".gephi");
        File gml2File = new File(System.getProperty("user.dir") + "/compare4/" + ConferenceID1 + " " + ConferenceID2 + "_.gml");



        ec.exportFile(svgFile);
        ec.exportFile(pngFile);
        ec.exportFile(gml2File);
        //pc.saveProject(pc.getCurrentProject(), gephiFile).run();
        
        //GraphView view2 = graphModel.getVisibleView();
        

        
		InputStream is = null;
		OutputStream os = null;

		is = new FileInputStream(System.getProperty("user.dir") + "/compare4/" + ConferenceID1 + " "+ ConferenceID2 + ".svg");
		os = new FileOutputStream(System.getProperty("user.dir") + "/compare4/" + ConferenceID1 + " "+ ConferenceID2 + ".php");

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

        
        //generateSvg("4372AEEF",1); 
        //generateSvg("442BD7CD",1);
        //generateSvg("42F4F2CC",1);
        //generateSvg("44B13001",1);
        //generateSvg("46A355BA",1);
        /*generateSvg("469BDC4B",1);
        
        generateSvg("43319DD4",1);
        generateSvg("45FB3662",1);
        generateSvg("43ABF249",1);
        generateSvg("436976F3",1);*/
        //generateSvg("43ABF249",1);
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
       
      /* generateSvg("442BD7CD","42F4F2CC");
       generateSvg("44B13001","42F4F2CC");
       generateSvg("442BD7CD","44B13001");
       generateSvg("43226B44","436EE6FE");
       generateSvg("43226B44","44616FF1");
       generateSvg("43226B44","4499A295");
       generateSvg("43226B44","45FB3662");
       generateSvg("44616FF1","4499A295");
       generateSvg("44616FF1","45FB3662");
       generateSvg("4499A295","45FB3662");
       generateSvg("436EE6FE","44616FF1");
       generateSvg("436EE6FE","4499A295");
       generateSvg("436EE6FE","45FB3662");
       
       generateSvg("43F6459B","44C16141");
       generateSvg("43F6459B","455B6732");
       generateSvg("43F6459B","476F3906");
       generateSvg("455B6732","476F3906");
       generateSvg("44C16141","455B6732");
       generateSvg("44C16141","476F3906");
       generateSvg("460A7036","45610CDA");
       generateSvg("460A7036","43FD776C");
       generateSvg("460A7036","4390334E");
       generateSvg("45610CDA","43FD776C");
       generateSvg("45610CDA","4390334E");
       generateSvg("43FD776C","4390334E");
       
       generateSvg("448ECA1D","4549948F");
       generateSvg("448ECA1D","45CC165B");
       generateSvg("448ECA1D","46FBD884");
       generateSvg("4549948F","45CC165B");
       generateSvg("4549948F","46FBD884");
       generateSvg("45CC165B","46FBD884");

       generateSvg("4566232D","443F9CFF");*/
       
      /*generateSvg("43ABF249","4306EF42");
       
       generateSvg("45D14EDE","4558D729");
       generateSvg("45D14EDE","473B0162");
       generateSvg("4558D729","473B0162");
       generateSvg("46A355BA","45FFFB88");
       generateSvg("46A355BA","45CEC493");
       generateSvg("46A355BA","45878F67");
       generateSvg("46A355BA","4524B15F");
       generateSvg("46A355BA","43DFAE0A");
       generateSvg("46A355BA","4332078E");
       generateSvg("45FFFB88","45CEC493");
       generateSvg("45FFFB88","45878F67");
       generateSvg("45FFFB88","4524B15F");
       generateSvg("45FFFB88","43DFAE0A");
       generateSvg("45FFFB88","4332078E");
       generateSvg("45CEC493","45878F67");
       generateSvg("45CEC493","4524B15F");
       generateSvg("45CEC493","43DFAE0A");
       generateSvg("45CEC493","4332078E");
       generateSvg("45878F67","4524B15F");
       generateSvg("45878F67","43DFAE0A");
       generateSvg("45878F67","4332078E");
       generateSvg("4524B15F","43DFAE0A");
       generateSvg("4524B15F","473B0162");
       generateSvg("43DFAE0A","4332078E");
       generateSvg("47C39427","46DAB993");
       generateSvg("47C39427","46A05BB0");
       generateSvg("47C39427","465F7C62");
       generateSvg("47C39427","43319DD4");
       generateSvg("47C39427","45083D2F");
       generateSvg("47C39427","45701BF3");
       generateSvg("46DAB993","46A05BB0");
       generateSvg("46DAB993","465F7C62");
       generateSvg("46DAB993","43319DD4");
       generateSvg("46DAB993","45083D2F");
       generateSvg("46DAB993","45701BF3");
       generateSvg("46A05BB0","465F7C62");
       generateSvg("46A05BB0","43319DD4");
       generateSvg("46A05BB0","45083D2F");
       generateSvg("46A05BB0","45701BF3");
       generateSvg("465F7C62","43319DD4");
       generateSvg("465F7C62","45083D2F");
       generateSvg("465F7C62","45701BF3");
       generateSvg("43319DD4","45083D2F");
       generateSvg("43319DD4","45701BF3");
       generateSvg("45083D2F","45701BF3");
       generateSvg("436EE6FE","43226B44");
       generateSvg("4499A295","43226B44");
       generateSvg("45FB3662","43226B44");
       generateSvg("4499A295","44616FF1");
       generateSvg("45FB3662","44616FF1");
       generateSvg("4499A295","436EE6FE");
       generateSvg("45FB3662","436EE6FE");
       generateSvg("44C16141","43F6459B");
       generateSvg("476F3906","43F6459B");
       generateSvg("476F3906","455B6732");
       generateSvg("44C16141","455B6732");
       generateSvg("44C16141","455B6732");
       generateSvg("46FBD884","448ECA1D");
       generateSvg("4549948F","45CC165B");
       generateSvg("46FBD884","4549948F");*/
       generateSvg("46FBD884","45CC165B");
       generateSvg("436976F3","45610CDA");
       generateSvg("436976F3","43FD776C");
       generateSvg("436976F3","4390334E");
       
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

