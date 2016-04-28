package annpud.mybatis.entity;

/**
 * 需要处理的主键信息
 * 
 */
public class PkField {

	// 数据库列名
	private String dbColumnName;
	// 实体类字段名
	private String entityField;

	public PkField() {
		this.dbColumnName = "id";
		this.entityField = this.dbColumnName;
	}

	public final String getDbColumnName() {
		return dbColumnName;
	}

	public final void setDbColumnName(String dbColumnName) {
		this.dbColumnName = dbColumnName;
	}

	public final String getEntityField() {
		return entityField;
	}

	public final void setEntityField(String entityField) {
		this.entityField = entityField;
	}

}
