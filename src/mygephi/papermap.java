//这是用来画领域内论文地图的，代码原作者唐炜杰
package mygephi;

import myclass.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFrame;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.layout.plugin.noverlap.NoverlapLayout;
import org.gephi.layout.plugin.noverlap.NoverlapLayoutBuilder;
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
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.toolkit.demos.plugins.preview.PreviewSketch;
import org.openide.util.Lookup;


public class papermap {
	private static Statement stmt = null, stmt2 = null;   //Statement是一个接口，提供了向数据库发送执行语句和获取结果的方法
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
	
	public static void generateGml(String fieldOfStudyID) {
		try{
            //String sql = "select * from FieldsOfStudyCount WHERE FieldsOfStudyID = '" + fieldOfStudyID + "'";    //要执行的SQL
            String sql = "SELECT * FROM PaperRefStats WHERE FieldsOfStudyID='" + fieldOfStudyID + "'";
            ResultSet rs = stmt.executeQuery(sql);
            int numberOfPapers = 200; //number of papers to show before filtered
            double delta = 0.1;
            int totalAmountOfPapers = 0;
            String fieldOfStudyName = fieldOfStudyID;
            int qualified = 0;
            while (rs.next()){
            	totalAmountOfPapers = rs.getInt("TotalAmountOfPapers");
            	//fieldOfStudyName = rs.getString("FieldsOfStudyName");
                qualified = rs.getInt("PapersQualified");
            	//numberOfPapers = Math.min((int) Math.min(totalAmountOfPapers, 5 * Math.sqrt(totalAmountOfPapers)), numberOfPapers);
                //numberOfPapers = Math.min(totalAmountOfPapers, numberOfPapers);
            }
            rs.close();
            
            if (qualified > numberOfPapers*delta && totalAmountOfPapers>numberOfPapers) {
                qualified = (int) (numberOfPapers*delta);
            }
            numberOfPapers = Math.min(totalAmountOfPapers, numberOfPapers);
            
            System.out.println("(" + numberOfPapers + "/" + qualified + ")" + " papers out of " + totalAmountOfPapers + " in the field of " + fieldOfStudyName + "(" + fieldOfStudyID + ") will be displayed.");
            
            sql =       "(SELECT TB1.PaperID,ReferenceCount \n" +
                        "FROM \n" +
                        "	(SELECT DISTINCT PaperID FROM PaperKeywords WHERE FieldOfStudyIDMappedToKeyword='"+fieldOfStudyID+"') AS TB1 \n" +
                        "	INNER JOIN paperreferencescount \n" +
                        "	ON TB1.PaperID = paperreferencescount.PaperReferenceID \n" +
                        "ORDER BY ReferenceCount DESC LIMIT "+numberOfPapers+") \n" +
                        "UNION \n" +
                        "(SELECT DISTINCT PaperID,FieldCitation \n" +
                        "FROM PaperRefKeywords \n" +
                        "WHERE FieldOfStudyIDMappedToKeyword='"+fieldOfStudyID+"' \n" +
                        "ORDER BY FieldCitation DESC LIMIT "+qualified+")";   //PaperID
            rs = stmt.executeQuery(sql);
            System.out.println("Retrieved papers id successfully.");
            
            HashMap<String , Integer> hashMap = new HashMap<String , Integer>();
            String id="";
            Integer count=0,total=0;
            while (rs.next()) {
                
                    id = rs.getString("PaperID");
                    //count = Integer.parseInt(rs.getString("CNT"));
                    //count = (int) Math.log(count);
                    hashMap.put(id, count);
                    total++;
                
            }
            rs.close(); 
            System.out.println("Retrieved "+ total + " papers id successfully.");
            
            OutputStream os = null;
            try {
                    os = new FileOutputStream(System.getProperty("user.dir") + "/yyy/" + fieldOfStudyID + ".gml");//创建一个文件路径
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("graph [\n");
            writer.write("directed 1\n");
            
            Integer limit = 10000;
            Paper[] papers = new Paper[limit+100];
            
            HashSet<String> hashSet = new HashSet<String>();
            
            Iterator iter = hashMap.entrySet().iterator();//map.entrySet()是把HashMap类型的数据转换成集合类型 map.entrySet().iterator()是去获得这个集合的迭代器，保存在iter里面。
            int cnt=0;
            int totalprocess=0;
            while (iter.hasNext()) {//若仍有元素可以迭代就返回true
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();//不确定传进来的是什么类就用这个笼统的
                Object val = entry.getValue();
                
                //Integer numberOfPapers1 = (int)Math.min(Integer.parseInt(val.toString()), Integer.parseInt(val.toString())*numberOfPapers/total);
                if (numberOfPapers>=0) {
                  
                System.out.println(key+": "+ ++totalprocess);
                
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
                sql =   "SELECT * FROM \n" +
                        "	(SELECT AuthorName FROM Authors INNER JOIN \n" +
                        "	(SELECT AuthorID FROM PaperAuthorAffiliations WHERE PaperID='"+key+"' AND AuthorSequenceNumber=1) AS TB1 \n" +
                        "	ON TB1.AuthorID=Authors.AuthorID) AS TB2 \n" +
                        "INNER JOIN \n" +
                        "	(SELECT PaperID,PaperPublishYear FROM Papers WHERE PaperID='"+key+"') AS TB3 ";
                rs = stmt.executeQuery(sql);
                //System.out.println("Retrieved papers successfully.");

                while (rs.next()) {
                    if (!hashSet.contains(key.toString()))
                    {
                        cnt++;
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
                        writer.write("year " + rs.getString("PaperPublishYear") + "\n]\n");
                    }
                }
                }
            }
           Iterator<String> iterator= hashSet.iterator();
            
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
            		}
            	}
            	rs.close();
            }
        
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
//                String tmp2 = rs.getString("PaperID");
//                if (hashSet.contains(tmp1) && hashSet.contains(tmp2) && !tmp1.equals(tmp2)) {
//                    writer.write("edge [\n");
//                    writer.write("source \"" + tmp2 + "\"\n");
//                    writer.write("target \"" + tmp1 + "\"\n]\n");
//                }
//            }
            rs.close();
        /*
            String post = "";
            for(Iterator it=hashSet.iterator();it.hasNext();) {
                post += it.next();
                post += ",";
            }
            String[] results = HttpRequest.sendPost("http://202.120.36.137:10080",post).split(";");
            System.out.println(results);
            for (String result : results) {
                String tmp1 = result.split(",")[0];
                String tmp2 = result.split(",")[1];
                if (!tmp1.equals(tmp2)) {
                    writer.write("edge [\n");
                    writer.write("source \"" + tmp1 + "\"\n");
                    writer.write("target \"" + tmp2 + "\"\n]\n");
                }
            }
           
            writer.write("]");
            writer.close();
            os.close();
            */
            System.out.println("Content written to " + fieldOfStudyID + ".gml");
       } catch(Exception e) {
    	   e.printStackTrace();
       }
	}
	
	public static void generateSvg(String fieldOfStudyID) throws IOException, SQLException {
		/*File file = new File(System.getProperty("user.dir") + "/papermaps/" + fieldOfStudyID + ".php");
		if (file.exists()) {
			System.out.println(fieldOfStudyID + " already exists");
			return;
		}*/
		generateGml(fieldOfStudyID);
		
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
        File gmlFile = new File(System.getProperty("user.dir") + "/yyy/" + fieldOfStudyID + ".gml");
        try {
            container = importController.importFile(gmlFile);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);   //Force DIRECTED
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
        System.out.println("Nodes: " + graphVisible.getNodeCount());
        System.out.println("Edges: " + graphVisible.getEdgeCount());
        
        if (graphVisible.getNodeCount() == 0) {
        	System.out.println("No node!");
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
        Column yearCol = graphModel.getNodeTable().getColumn("year");
        Column nodeSizeCol = graphModel.getNodeTable().addColumn("Size", Double.class);
        Double year,nodeIndgree,nodeSize;
        for (Node n : graphModel.getDirectedGraph().getNodes()) { 
            year = Double.parseDouble(n.getAttribute(yearCol).toString());
            nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
            nodeSize = 0.5 * nodeIndgree + 0.5 * nodeIndgree * Math.exp((year-2015)/10);
            //nodeSize *= nodeSize;
            n.setAttribute(nodeSizeCol, nodeSize);
            n.setAttribute(yearCol, year);
        }
        //Set edge weight
        Double sourceYear,targetYear;
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            sourceYear = Double.parseDouble(e.getSource().getAttribute(yearCol).toString());
            targetYear = Double.parseDouble(e.getTarget().getAttribute(yearCol).toString());
            if (Math.abs(sourceYear - targetYear) < 20) 
                e.setWeight(20 - Math.abs(sourceYear - targetYear)); 
            else
                e.setWeight(1.0);
        }
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
            e.setWeight(tmpweight);
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
        
        HashMap<String, Color> yearColor = new HashMap();
        yearColor.put("1986.0", new Color(0x0426E2));
        yearColor.put("1987.0", new Color(0x142EE0));
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
        yearColor.put("2016.0", new Color(0xFF0000));
        
        Iterator<Node> nodeIter = graphModel.getGraphVisible().getNodes().iterator();
        while (nodeIter.hasNext()) {
            Node node = nodeIter.next();//node.setFixed(true);
            if ((int)(Double.parseDouble(node.getAttribute("year").toString()))<1986) node.setColor(new Color(0x0426E2));
            else node.setColor(yearColor.get(node.getAttribute("year").toString()));
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
        nodeSizeCol = graphModel.getNodeTable().getColumn("Size");
        Function sizeRanking = appearanceModel.getNodeFunction(graph, nodeSizeCol, RankingNodeSizeTransformer.class);
        if (sizeRanking == null){
            System.out.println("----NodeSizeRanking failed----");
            //Rank size by inDegree
            Function degreeRanking2 = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_INDEGREE, RankingNodeSizeTransformer.class);
            RankingNodeSizeTransformer degreeTransformer2 = (RankingNodeSizeTransformer) degreeRanking2.getTransformer();
            degreeTransformer2.setMaxSize(200);
            degreeTransformer2.setMinSize(20);
            appearanceController.transform(degreeRanking2);
        }
        else{
            RankingNodeSizeTransformer sizeTransformer = (RankingNodeSizeTransformer) sizeRanking.getTransformer();
            sizeTransformer.setMaxSize(200);
            sizeTransformer.setMinSize(20);
            appearanceController.transform(sizeRanking);
        }
        
        //ForceAtlas
        ForceAtlasLayout faLayout = new ForceAtlasLayout(null);
        faLayout.setGraphModel(graphModel);
        faLayout.initAlgo();
        faLayout.resetPropertiesValues();
        //System.out.println(faLayout.getSpeed());
        faLayout.setRepulsionStrength(Math.min(6000.0, Math.max(100.0, (double)graphVisible.getNodeCount()*10)));
        faLayout.setMaxDisplacement(200.0);
        faLayout.setGravity(0.0);
        faLayout.setOutboundAttractionDistribution(Boolean.TRUE);
        faLayout.setSpeed(3.0);
        
        for (int i = 0; i < 2000 && faLayout.canAlgo(); i++) {
            faLayout.goAlgo(); //System.out.print(i);
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
        
        /*//ForceAtlas2
        ForceAtlas2 fa2Layout = new ForceAtlas2(new ForceAtlas2Builder());
       fa2Layout.setGraphModel(graphModel);
       fa2Layout.resetPropertiesValues();
       fa2Layout.setEdgeWeightInfluence(1.0);
       fa2Layout.setGravity(1.0);
       fa2Layout.setScalingRatio(2.0);
        fa2Layout.setBarnesHutTheta(1.2);
       fa2Layout.setJitterTolerance(0.1);
       fa2Layout.initAlgo();
        fa2Layout.setAdjustSizes(true);
       for (int i = 0; i < 1000 && fa2Layout.canAlgo(); i++) 
      	fa2Layout.goAlgo();
       fa2Layout.endAlgo();*/
      
        /*//Filter
        InDegreeRangeFilter inDegreeFilter = new InDegreeRangeFilter();
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
        //model.getProperties().putValue(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.TRUE);        
        //model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(2.0f));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, new java.awt.Font("Dialog", 0, 2));
        model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
        //previewController.refreshPreview();

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        File svgFile = new File(System.getProperty("user.dir") + "/yyy/" + fieldOfStudyID + ".svg");
        File pngFile = new File(System.getProperty("user.dir") + "/yyy/" + fieldOfStudyID + ".png");
        File gephiFile = new File(System.getProperty("user.dir") + "/yyy/" + fieldOfStudyID + ".gephi");
        File gmlFile1 = new File(System.getProperty("user.dir") + "/yyy/" + fieldOfStudyID + "_.gml");

        ec.exportFile(svgFile);
        ec.exportFile(pngFile);
        pc.saveProject(pc.getCurrentProject(), gephiFile).run();
        
        //GraphView view2 = graphModel.getVisibleView();

        
		InputStream is = null;
		OutputStream os = null;

		is = new FileInputStream(System.getProperty("user.dir") + "/yyy/" + fieldOfStudyID + ".svg");
		os = new FileOutputStream(System.getProperty("user.dir") + "/yyy/" + fieldOfStudyID + ".php");

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
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
        System.out.println("成功连接到数据库！");
        
      /*  String sql = "select tb.ChildFieldOfStudyID as FieldsOfStudyID,FieldsOfStudyCount.FieldsOfStudyName,FieldsOfStudyCount.TotalAmountOfPapers from FieldsOfStudyCount inner join "
                + "(select distinct ChildFieldOfStudyID from FieldOfStudyHierarchy where ChildFieldOfStudyLevel='L1') as tb "
                + "on tb.ChildFieldOfStudyID=FieldsOfStudyID order by TotalAmountOfPapers desc limit 230";*/
       //String sql = "SELECT * FROM FieldsOfStudyCount having TotalAmountOfPapers >= 100000";
        String sql = "SELECT * FROM FieldsOfStudyCount";
        ResultSet rs2 = stmt2.executeQuery(sql);            // executeQuery会返回结果的集合，否则返回空值
        String fieldsOfStudyID = "";
        String fieldsOfStudyName = "";
        int totalAmountOfPapers = 0;
        int cnt = 0;
        while (rs2.next()) {
			try {
				fieldsOfStudyID = rs2.getString("FieldsOfStudyID");
				fieldsOfStudyName = rs2.getString("FieldsOfStudyName");
				totalAmountOfPapers = rs2.getInt("TotalAmountOfPapers");
				System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				System.out.println(fieldsOfStudyName);
				System.out.println(totalAmountOfPapers);
	        	writer.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
	        	writer.newLine();
	        	writer.write(fieldsOfStudyName);
	        	writer.newLine();
	        	writer.write(totalAmountOfPapers);
	        	writer.newLine();
				generateSvg(fieldsOfStudyID);
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
        generateSvg("01E7DD16"); 
	stmt.close();
        conn.close();
        stmt2.close();
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