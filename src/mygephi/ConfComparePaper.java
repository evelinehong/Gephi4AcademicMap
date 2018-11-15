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
import static mygephi.yyy4.toHexFromColor;

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


public class ConfComparePaper {
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
	
	public static void generateGml(String ConferenceID1, String ConferenceID2) {
		try{

            String ConferenceName1 = ConferenceID1;
            String ConferenceName2 = ConferenceID2;
            int qualified = 0;

            /*String sql =       "SELECT TB1.PaperID,ReferenceCount \n" +
                        "FROM \n" +
                        "	(SELECT DISTINCT PaperID FROM Papers WHERE ConferenceSeriesIDMappedToVenueName='"+ConferenceID1+"' OR ConferenceSeriesIDMappedToVenueName='"+ConferenceID2+"' OR ConferenceSeriesIDMappedToVenueName='"+ConferenceID3+"') AS TB1 \n" +
                        "	INNER JOIN PaperReferenceCount \n" +
                        "	ON TB1.PaperID = PaperReferenceCount.PaperID \n" +
                        "ORDER BY ReferenceCount DESC LIMIT 1500" ;

            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Retrieved papers id successfully.");
 
            HashMap<String , Integer> hashMap = new HashMap<String , Integer>();
            String id="";
            Integer count=0,total=0;
            while (rs.next()) {
                
                    id = rs.getString("PaperID");
                    count = rs.getInt("ReferenceCount");
                    //count = Integer.parseInt(rs.getString("CNT"));
                    hashMap.put(id, count);
                    total++;
                
            }*/
            String sql =       "SELECT TB1.PaperID,CitationCount \n" +
                        "FROM \n" +
                        "	(SELECT DISTINCT PaperID FROM Papers WHERE ConferenceSeriesIDMappedToVenueName='"+ConferenceID1+"' ) AS TB1 \n" +
                        "	INNER JOIN PaperCitationCount \n" +
                        "	ON TB1.PaperID = PaperCitationCount.PaperID \n" +
                        "ORDER BY CitationCount DESC LIMIT 500" ;

            ResultSet rs = stmt.executeQuery(sql);
 
            HashMap<String , Integer> hashMap = new HashMap<String , Integer>();
            String id="";
            Integer count=0,total=0;
            while (rs.next()) {
                
                    id = rs.getString("PaperID");
                    count = rs.getInt("CitationCount");
                    //count = Integer.parseInt(rs.getString("CNT"));
                    hashMap.put(id, count);
                    total++;
                
            }
            rs.close(); 
            sql =       "SELECT TB1.PaperID,CitationCount \n" +
                        "FROM \n" +
                        "	(SELECT DISTINCT PaperID FROM Papers WHERE ConferenceSeriesIDMappedToVenueName='"+ConferenceID2+"' ) AS TB1 \n" +
                        "	INNER JOIN PaperCitationCount \n" +
                        "	ON TB1.PaperID = PaperCitationCount.PaperID \n" +
                        "ORDER BY CitationCount DESC LIMIT 500" ;

            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                
                    id = rs.getString("PaperID");
                    count = rs.getInt("CitationCount");
                    //count = Integer.parseInt(rs.getString("CNT"));
                    hashMap.put(id, count);
                    total++;
                
            }
            rs.close();
           
            System.out.println("Retrieved "+ total + " papers id successfully.");
            
            OutputStream os = null;
            try {
                    os = new FileOutputStream(System.getProperty("user.dir") + "/compaper4/" + ConferenceID1 + " " +ConferenceID2  + ".gml");//创建一个文件路径
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("graph [\n");
            writer.write("directed 1\n");

            HashSet<String> hashSet = new HashSet<String>();
            
            Iterator iter = hashMap.entrySet().iterator();//map.entrySet()是把HashMap类型的数据转换成集合类型 map.entrySet().iterator()是去获得这个集合的迭代器，保存在iter里面。
            int cnt=0;
            int totalprocess=0;
            while (iter.hasNext()) {//若仍有元素可以迭代就返回true
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();//不确定传进来的是什么类就用这个笼统的
                Object val = entry.getValue();

                System.out.println(key+": "+ ++totalprocess);

                sql =   "SELECT * FROM \n" +
                        "	(SELECT AuthorName FROM Authors INNER JOIN \n" +
                        "	(SELECT AuthorID FROM PaperAuthorAffiliations WHERE PaperID='"+key+"' AND AuthorSequenceNumber=1) AS TB1 \n" +
                        "	ON TB1.AuthorID=Authors.AuthorID) AS TB2 \n" +
                        "INNER JOIN \n" +
                        "	(SELECT PaperID,PaperPublishYear, ConferenceSeriesIDMappedToVenueName, NormalizedPaperTitle, CitationCount FROM Papers WHERE PaperID='"+key+"') AS TB3 ";
                rs = stmt.executeQuery(sql);
                //System.out.println("Retrieved papers successfully.");

                while (rs.next()) {
                    if (!hashSet.contains(key.toString()))
                    {
                        cnt++;
                        writer.write("node [\n");
                        hashSet.add(key.toString());
                        writer.write("id \"" + key.toString() + "\"\n");
                        writer.write("label \"" + capitalize(rs.getString("AuthorName")) + "\"\n");
                        writer.write("conference " + rs.getString("ConferenceSeriesIDMappedToVenueName") + "\n");
                        writer.write("citation " + Double.parseDouble(rs.getString("CitationCount")) + "\n");
                        writer.write("title \"" +rs.getString("NormalizedPaperTitle") +"\"\n");
                        writer.write("size " + (Double.parseDouble(rs.getString("CitationCount"))+1) + "\n");
                        writer.write("year " + rs.getString("PaperPublishYear") + "\n]\n");
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
                                System.out.println(paperID + ++cnt);
            		}
            	}
            	rs.close();
            }
            writer.write("]");
            writer.close();
            os.close();
            rs.close();

            System.out.println("Content written to compare map");
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
        File gmlFile = new File(System.getProperty("user.dir") + "/compaper4/" + ConferenceID1 + " " +ConferenceID2   + ".gml");
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
        degreeFilter.setRange(new Range(0, Integer.MAX_VALUE));     //Remove nodes with degree = 0
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
        DirectedGraph graphVisible = graphModel.getDirectedGraphVisible();
        System.out.println("Nodes: " + graphVisible.getNodeCount());
        System.out.println("Edges: " + graphVisible.getEdgeCount());
        
        if (graphVisible.getNodeCount() == 0) {
        	System.out.println("No node!");
        	gmlFile.delete();
        	return;
        }
        //System.out.println(pc.getCurrentWorkspace());

        //Add indgree column
        
        //Add node size column
        Column yearCol = graphModel.getNodeTable().getColumn("year");
        Column conferenceCol = graphModel.getNodeTable().getColumn("conference");
        Column nodeSizeCol = graphModel.getNodeTable().getColumn("size");
        Double year,nodeyear,nodeSize,size,totalsize1=0.0,totalsize2=0.0,totalsize3=0.0,totalsize4=0.0,maxsize=0.0,minsize = 10000.0;
        String ConferenceID,conference;
        Double maxyear = 0.0, minyear = 10000.0;
        for (Node n : graphModel.getDirectedGraph().getNodes()) { 
            year = Double.parseDouble(n.getAttribute(yearCol).toString());
            size = Double.parseDouble(n.getAttribute(nodeSizeCol).toString());
            if(year <=minyear) minyear = year;
            if(year >=maxyear) maxyear = year;
            nodeSize = size;
            nodeyear = year;
            conference = n.getAttribute(conferenceCol).toString();
            ConferenceID = conference;
            n.setAttribute(yearCol,nodeyear);
            n.setAttribute(conferenceCol, ConferenceID);
            n.setAttribute(nodeSizeCol, nodeSize);
            if(conference.equals(ConferenceID1)) {totalsize1 = totalsize1 + graphModel.getDirectedGraph().getInDegree(n);totalsize3 = totalsize3+size;}
            else if(conference.equals(ConferenceID2)) {totalsize2 = totalsize2 + graphModel.getDirectedGraph().getInDegree(n); totalsize4 = totalsize4+size;}
        
           
            if(size>=maxsize) maxsize = size;
            if(size<=minsize) minsize = size;
        }
        System.out.println(maxyear);
        System.out.println(minyear);
        Double yeara = 105/(maxyear-minyear);
        Double yearb = 255-yeara*maxyear;
        Double sizea = 550/(maxsize-minsize);
        Double sizeb = 600-sizea*maxsize;
        System.out.println(totalsize1);
        System.out.println(totalsize2);
        System.out.println(totalsize3);
        System.out.println(totalsize4);
        
        Column titleCol = graphModel.getNodeTable().getColumn("title");
        String titlename;
        Double citation;
        Column citationCol = graphModel.getNodeTable().getColumn("citation");
        Column opaCol = graphModel.getNodeTable().addColumn("stroke-opacity", Double.class);
        for (Node n : graphModel.getDirectedGraph().getNodes()) { 
            year = Double.parseDouble(n.getAttribute(yearCol).toString());
            int color = (int)(yeara*year + yearb);
            conference = n.getAttribute(conferenceCol).toString();
            if(conference.equals(ConferenceID1) && totalsize3 >= totalsize4) n.setColor(new Color(color,0,0));
            else if(conference.equals(ConferenceID1) && totalsize4 >= totalsize3) n.setColor(new Color(0,color,0));
            else if(conference.equals(ConferenceID2) && totalsize3 <= totalsize4) n.setColor(new Color(color,0,0));
            else if(conference.equals(ConferenceID2) && totalsize3 >= totalsize4) n.setColor(new Color(0,color,0));
            n.setAlpha((float)color/100);
            n.setAttribute(opaCol,(double)color/100);
            titlename = n.getAttribute(titleCol).toString();
            citation = Double.parseDouble(n.getAttribute(citationCol).toString());
            n.setAttribute(titleCol, titlename);
            n.setAttribute(citationCol, citation);
        }
       
        
        //Set edge weight
        Double sourceYear,targetYear;
        for (Edge e : graphModel.getDirectedGraph().getEdges()) {
            sourceYear = Double.parseDouble(e.getSource().getAttribute(yearCol).toString());
            targetYear = Double.parseDouble(e.getTarget().getAttribute(yearCol).toString());
            if (Math.abs(sourceYear - targetYear) < 20) 
                e.setWeight(Math.log10(22 - Math.abs(sourceYear - targetYear))); 
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
            sizeTransformer.setMaxSize(1000);
            sizeTransformer.setMinSize(100);
            appearanceController.transform(sizeRanking);
        
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
       fa2Layout.setScalingRatio(Math.max(0.003*graphVisible.getNodeCount()+2.0,15.0));
       
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
        if (true){
            //Column colorCol = graphModel.getNodeTable().getColumn("fill");
            Column nodeRadiusCol = graphModel.getNodeTable().addColumn("Radius", Float.class);
            Column labelSizeCol = graphModel.getNodeTable().addColumn("labelsize", Float.class);
            float radius, nodeRadius;
            float labelsize,labelSize;
            
            for (Node n : graphModel.getDirectedGraph().getNodes()) {
                radius = n.size();
                labelsize = radius * 3 / 4;
                //nodeIndgree = Double.parseDouble(n.getAttribute(indgreeCol).toString());
                nodeRadius = radius;
                labelSize = labelsize;
                n.setAttribute(nodeRadiusCol, nodeRadius);
                n.setAttribute(labelSizeCol, labelSize);
            }
        }
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
       

        
        //Preview
      //  PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        //model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(new Color(186, 172, 212)));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.SHOW_EDGES, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float (20.0));
        model.getProperties().putValue(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.FALSE);        
        //model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(2.0f));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, new java.awt.Font("Dialog", 0, 2));
        model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
        //previewController.refreshPreview();

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        File svgFile = new File(System.getProperty("user.dir") + "/compaper4/" + ConferenceID1+ " " + ConferenceID2  + ".svg");
        File pngFile = new File(System.getProperty("user.dir") + "/compaper4/" + ConferenceID1 + " "+ ConferenceID2 + ".png");
        File gmlFile1 = new File(System.getProperty("user.dir") + "/compaper4/" + ConferenceID1 + " "+ ConferenceID2 + "_" + ".gml");

        ec.exportFile(svgFile);
        ec.exportFile(pngFile);
        ec.exportFile(gmlFile1);
        //GraphView view2 = graphModel.getVisibleView();

        
		InputStream is = null;
		OutputStream os = null;

		is = new FileInputStream(System.getProperty("user.dir") + "/compaper4/" + ConferenceID1+ " " + ConferenceID2  + ".svg");
		os = new FileOutputStream(System.getProperty("user.dir") + "/compaper4/" + ConferenceID1+ " " + ConferenceID2  + ".php");

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

	}

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
        /*String sql = "SELECT * FROM FieldsOfStudyCount WHERE FieldsOfStudyID='04984686'";
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
        rs2.close();*/
       /*generateSvg("442BD7CD","42F4F2CC");
       generateSvg("42F4F2CC","44B13001");
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
       generateSvg("45083D2F","45701BF3");*/
       generateSvg("436976F3","45610CDA");
       generateSvg("436976F3","43FD776C");
       generateSvg("436976F3","4390334E");
	stmt.close();
        conn.close();
        stmt2.close();
        writer.close();
        os.close();
	}
}
