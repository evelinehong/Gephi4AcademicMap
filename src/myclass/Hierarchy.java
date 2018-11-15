package myclass;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.String;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public  class Hierarchy {
    private static Statement stmt = null;   //Statement是一个接口，提供了向数据库发送执行语句和获取结果的方法
    private static Connection conn = null;//Connection是用于将JAVA和数据库连接的类
    
    public  static class hierarchyTree{
        private class node{
            String fieldID;
            String fieldLevel;
            int parentIndex;
            public node()
            {fieldID="";
            fieldLevel="";
            parentIndex=0;
            }
        }
        private node[] data;
        private int currentlength;
        public hierarchyTree()
        {currentlength=1;
         data=new node[80000];
         for (int i=0;i<80000;++i)
         data[i]=new node();
         data[0].fieldID="";
         data[0].fieldLevel="";
         data[0].parentIndex=0;
        }
        
    
        private void insert(String ID,String level,int order)
        {data[currentlength].fieldID=ID;
         data[currentlength].parentIndex=order;
         data[currentlength].fieldLevel=level;
         ++currentlength;}
        
        public void createTree()throws SQLException, ClassNotFoundException, IOException
        {   int operatelength1=0;//begin;
            int operatelength2=0;//end;
            String sql="SELECT DISTINCT ParentFieldOfStudyID,ParentFieldOfStudyLevel FROM `mag-new-160205`.FieldOfStudyHierarchy WHERE ParentFieldOfStudyLevel=\"L0\"";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {insert(rs.getString("ParentFieldOfStudyID"),"L0",0);}
            rs.close();
            operatelength1=1;
            operatelength2=currentlength;
            int l1begin=operatelength2;
            System.out.println("L0 finish");
            
            int i=0;
            for (i=1;i<operatelength2;++i)
            {sql="SELECT * FROM `mag-new-160205`.FieldOfStudyHierarchy WHERE ParentFieldOfStudyID=\""+data[i].fieldID+"\"AND ChildFieldOfStudyLevel=\"L1\"";
             rs=stmt.executeQuery(sql);
             while (rs.next())
             {insert(rs.getString("ChildFieldOfStudyID"),rs.getString("ChildFieldOfStudyLevel"),i);}
             rs.close();}
             operatelength1=operatelength2;
             operatelength2=currentlength;
             int l2begin=operatelength2;
             System.out.println("L1 finish");
             
             loop1:
             while (i<operatelength2)
             {for (int j=operatelength1;j<i;++j)
             { if (data[i].fieldID.equals(data[j].fieldID))
             {++i;
             continue loop1;}}
             sql="SELECT * FROM `mag-new-160205`.FieldOfStudyHierarchy WHERE ParentFieldOfStudyID=\""+data[i].fieldID+"\"AND ChildFieldOfStudyLevel=\"L2\"";
             rs=stmt.executeQuery(sql);
             while (rs.next())
             {insert(rs.getString("ChildFieldOfStudyID"),rs.getString("ChildFieldOfStudyLevel"),i);}
             rs.close();
             ++i;}
             System.out.println("L2 finish");
             
             operatelength1=operatelength2;
             operatelength2=currentlength;
             int l3begin=operatelength2;
             
             loop2:
             while (i<operatelength2)
             {for (int j=operatelength1;j<i;++j)
             {if (data[i].fieldID.equals(data[j].fieldID))
             {++i;
             continue loop2;}}
             sql="SELECT * FROM `mag-new-160205`.FieldOfStudyHierarchy WHERE ParentFieldOfStudyID=\""+data[i].fieldID+"\"AND ChildFieldOfStudyLevel=\"L3\"";
             rs=stmt.executeQuery(sql);
             while (rs.next())
             {insert(rs.getString("ChildFieldOfStudyID"),rs.getString("ChildFieldOfStudyLevel"),i);}
             rs.close();
             ++i;}
             operatelength1=operatelength2;
             operatelength2=currentlength;
             int l3end=currentlength;
             System.out.println("L3 finish");
             
             for (int j=1;j<l1begin;++j)
             {sql="SELECT * FROM `mag-new-160205`.FieldOfStudyHierarchy WHERE ParentFieldOfStudyID=\""+data[j].fieldID+"\"AND ChildFieldOfStudyLevel=\"L2\"";
              rs=stmt.executeQuery(sql);
              loop3:
             while (rs.next())
             {for(int m=l2begin;m<l3begin;++m)
             {if(rs.getString("ChildFieldOfStudyID").equals(data[m].fieldID))
                     continue loop3;}
             insert(rs.getString("ChildFieldOfStudyID"),rs.getString("ChildFieldOfStudyLevel"),j);}
              rs.close();}
             operatelength1=operatelength2;
             operatelength2=currentlength;
             
             loop4:
             for(i=operatelength1;i<operatelength2;++i)
             {for (int j=operatelength1;j<i;++j)
             {if (data[i].fieldID.equals(data[j].fieldID))
             {continue loop4;}}
             sql="SELECT * FROM `mag-new-160205`.FieldOfStudyHierarchy WHERE ParentFieldOfStudyID=\""+data[i].fieldID+"\"AND ChildFieldOfStudyLevel=\"L3\"";
             rs=stmt.executeQuery(sql);
             while (rs.next())
             {insert(rs.getString("ChildFieldOfStudyID"),rs.getString("ChildFieldOfStudyLevel"),i);}
             rs.close();
             }
             operatelength1=operatelength2;
             operatelength2=currentlength;
             System.out.println("L0-2-3 finish");
             
             loop5:
             for (int j=l1begin;j<l2begin;++j)
             {for (int n=l1begin;n<j;++n)
             {if (data[n].fieldID.equals(data[j].fieldID))
             {continue loop5;}}
             sql="SELECT * FROM `mag-new-160205`.FieldOfStudyHierarchy WHERE ParentFieldOfStudyID=\""+data[j].fieldID+"\"AND ChildFieldOfStudyLevel=\"L3\"";
              rs=stmt.executeQuery(sql);
              loop6:
             while (rs.next())
             {for(int m=l3begin;m<operatelength2;++m)
             { if(rs.getString("ChildFieldOfStudyID").equals(data[m].fieldID))
                     continue loop6;}
             insert(rs.getString("ChildFieldOfStudyID"),rs.getString("ChildFieldOfStudyLevel"),j);}
             rs.close();}
             operatelength1=operatelength2;
             operatelength2=currentlength;
             System.out.println("L1-3 finish");
             
             loop7:
             for (i=1;i<l1begin;++i)
             {
             sql="SELECT * FROM `mag-new-160205`.FieldOfStudyHierarchy WHERE ParentFieldOfStudyID=\""+data[i].fieldID+"\"AND ChildFieldOfStudyLevel=\"L3\"";
             rs=stmt.executeQuery(sql);
             loop8:
             while (rs.next())
             {for (int m=l3begin;m<operatelength2;++m)
             {if(rs.getString("ChildFieldOfStudyID").equals(data[m].fieldID))
                     continue loop8;}
             insert(rs.getString("ChildFieldOfStudyID"),rs.getString("ChildFieldOfStudyLevel"),i);}
             rs.close();} 
             System.out.println("L0-3 finish");}
             
        public String search(String aa)
        {//Queue<node>output=new LinkedList<node>();
         Stack<node>output=new Stack<node>();
        String result=new String();
        int length=0;
         for(int i=1;i<currentlength;++i)
        {if(data[i].fieldID.equals(aa))
        {output.push(data[i]);
        ++length;
        }}
        while(length!=0)
        {node tmp;
         tmp=output.pop();
         --length;
         result+=tmp.fieldID+"@"+tmp.fieldLevel+" ";
         for(int i=1;i<currentlength;++i)
        {if(data[i].fieldID.equals(data[tmp.parentIndex].fieldID))
        {output.push(data[i]);
        ++length;}
        }
        }
        return result;
        }

        public String match(String parentID,String childID)
        {String[] child;
        String field="";
        String result="";
        child=childID.split("\\|");
        int i=0;
        for(String s:child)
        {field=search(child[i]);
          if (field.indexOf(parentID)>0)
              result=result+child[i]+field.substring(8, 11)+"|";
          ++i;
        }
        return result;
        } }       
        
        public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {        
		
		Class.forName("com.mysql.jdbc.Driver");
                // or:
                // com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
                // or：
                // new com.mysql.jdbc.Driver();
		
        System.out.println("成功加载MySQL驱动！");
            
        String url="jdbc:mysql://202.120.36.137:6033/mag-new-160205";    //JDBC的URL URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值   
		try {
			conn = DriverManager.getConnection(url, "map","map"); // 一个Connection代表一个数据库连接
		} catch (SQLException e1) {
			e1.printStackTrace();
                        
		}
        try {
                stmt = conn.createStatement();    
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
        System.out.println("成功连接到数据库！");
        
        hierarchyTree exam=new hierarchyTree();
        String parentID,childID;
        parentID="0B0FEB68";
        childID="09E9B821|0AC440B6|0BCB2E99|034EC4EB|0000109A";
        exam.createTree();
        System.out.println(exam.match(parentID, childID));
        
    }}
