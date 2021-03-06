package com.yanan.framework.jdb.entity;

import java.util.List;

import com.yanan.framework.jdb.fragment.SqlFragment;

/**
 * sql映射mapper
 * @author yanan
 *
 */
public class SqlMappingMapper {
	//命名空间 
	private String namespace;
	//里面包含的语句片段
	private List<SqlFragment> sqlFragmentList;
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public List<SqlFragment> getSqlFragmentList() {
		return sqlFragmentList;
	}
	public void setSqlFragmentList(List<SqlFragment> sqlFragmentList) {
		this.sqlFragmentList = sqlFragmentList;
	}
	public SqlMappingMapper(String namespace) {
		super();
		this.namespace = namespace;
	}
	public SqlMappingMapper() {
		super();
	}
	@Override
	public String toString() {
		return "SqlMappingMapper [namespace=" + namespace + ", sqlFragmentList=" + sqlFragmentList + "]";
	}
}