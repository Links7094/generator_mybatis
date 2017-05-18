/**
 *    Copyright ${license.git.copyrightYears} the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package cc.bandaotixi.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.OneToOne;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

public class OneToOnePlugin extends PluginAdapter {
	private TableConfiguration getMapTc(String tableName,Context context){
		TableConfiguration tc = null;
		for (TableConfiguration t : context.getTableConfigurations()) {
			if (t.getTableName().equalsIgnoreCase(tableName)) {
				tc = t;
			}
		}
		return tc;
	}
	private String getModelPackage( IntrospectedTable introspectedTable,Context context){
		StringBuilder sb = new StringBuilder();
		sb.append(context.getJavaModelGeneratorConfiguration().getTargetPackage());
		sb.append(introspectedTable.getFullyQualifiedTable().getSubPackageForModel(StringUtility.isTrue(context.getJavaModelGeneratorConfiguration().getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))));
		String pakkage = sb.toString();
		return pakkage;
	}
	/**
	 * 修改model
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Context context=introspectedTable.getContext();
		for (OneToOne oto : introspectedTable.getOneToOnes()) {
			String tableName = oto.getMappingTable();
			TableConfiguration tc =getMapTc(tableName, context);
			if (tc != null) {
				String pakkage = getModelPackage(introspectedTable, context);
				String domainName = tc.getDomainObjectName();
				String type=pakkage+"."+domainName;
				String fieldName = domainName.replaceFirst(new String(new char[] { domainName.charAt(0) }), new String(new char[] { domainName.charAt(0) }).toLowerCase());
				org.mybatis.generator.api.dom.java.Field field = new org.mybatis.generator.api.dom.java.Field();
				
				field.setName(fieldName);
				FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(type);
				field.setType(fqjt);
				field.setVisibility(JavaVisibility.PRIVATE);
				topLevelClass.addImportedType(new FullyQualifiedJavaType(type));
				topLevelClass.addField(field);

				// get
				Method getMethod = new Method();
				getMethod.setVisibility(JavaVisibility.PUBLIC);
				getMethod.setReturnType(fqjt);
				getMethod.setName("get" + tc.getDomainObjectName());
				getMethod.addBodyLine("return " + fieldName + ";");
				topLevelClass.addMethod(getMethod);
				// set
				Method setMethod = new Method();
				setMethod.setVisibility(JavaVisibility.PUBLIC);
				setMethod.setName("set" + introspectedTable.getTableConfiguration().getDomainObjectName());
				setMethod.addParameter(new Parameter(new FullyQualifiedJavaType(type), fieldName));
				setMethod.addBodyLine("this." + fieldName + "=" + fieldName + ";");
				topLevelClass.addMethod(setMethod);
			}
		}
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 修改example
	 */
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 修改mapper
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	/**
	 * 修改mapper.xml
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		//获取
		Context context=introspectedTable.getContext();
		for (OneToOne oto : introspectedTable.getOneToOnes()) {
			String tableName = oto.getMappingTable();
			TableConfiguration tc =getMapTc(tableName, context);
			if (tc != null) {
				String domainName = tc.getDomainObjectName();
				String fieldName = domainName.replaceFirst(new String(new char[] { domainName.charAt(0) }), new String(new char[] { domainName.charAt(0) }).toLowerCase());
				// 添加<association property="teacher" column="teacher_id" select="getTeacher"/> 
				XmlElement assEle=new XmlElement("association");
				assEle.addAttribute(new Attribute("property", fieldName));
				assEle.addAttribute(new Attribute("column", oto.getColumn()));
				assEle.addAttribute(new Attribute("select", "get"+domainName));
				for(Element ele:document.getRootElement().getElements()){
					XmlElement xe=(XmlElement)ele;
					for(Attribute a:xe.getAttributes()){
						if(a.getName().equalsIgnoreCase("id")&&"BaseResultMap".equals(a.getValue())){
							xe.addElement(assEle);
						}
					}
				}
				String tuofengColum="";
				boolean isUp=false;
				for(byte b:oto.getColumn().getBytes()){
					char c=(char)b;
					if(c=='_'){
						isUp=true;
					}else{
						if(isUp){
							tuofengColum+=new String(new char[]{c}).toUpperCase();
							isUp=false;
						}else{
							tuofengColum+=c;
						}
					}
				}
				//添加查询方法<select id="testOutMapper" resultMap="soc.dao.ScanDao.BaseResultMap"><include refid="soc.dao.ScanDao.Base_Column_List" />
				XmlElement selectEle=new XmlElement("select");
				selectEle.addAttribute(new Attribute("id", "get"+domainName));
				selectEle.addAttribute(new Attribute("resultMap", introspectedTable.getMyBatis3SqlMapNamespace()+"."+"BaseResultMap"));
				String sql="select <include refid=\""+introspectedTable.getMyBatis3SqlMapNamespace()+".Base_Column_list\" /> from "+tableName+" where "+oto.getJoinColumn()+"=#{"+tuofengColum+"}";
				if(StringUtility.stringHasValue(oto.getWhere())){
					sql+=" and "+oto.getWhere();
				}
				selectEle.addElement(new TextElement(sql));
				document.getRootElement().addElement(selectEle);
			}
		}
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	@Override
	public boolean validate(List<String> warnings) {
		// TODO Auto-generated method stub
		return true;
	}

}