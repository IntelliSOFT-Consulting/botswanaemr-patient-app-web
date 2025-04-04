package com.botswana.botswanaemrfacility.database;

import com.botswana.botswanaemrfacility.DbJson;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Date;

@Entity
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
})
public class FacilityInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    private String fieldName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private DbJson dbJson;

    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;

    public FacilityInfo() {
    }

    public FacilityInfo(String fieldName, DbJson dbJson) {
        this.fieldName = fieldName;
        this.dbJson = dbJson;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public DbJson getDbJson() {
        return dbJson;
    }

    public void setDbJson(DbJson dbJson) {
        this.dbJson = dbJson;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}