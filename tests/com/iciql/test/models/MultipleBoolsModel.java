package com.iciql.test.models;

import java.util.Arrays;
import java.util.List;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

/**
 * Model class to test the runtime exception of too many primitive boolean
 * fields in the model.
 * 
 * @author James Moger
 * 
 */
@IQTable
public class MultipleBoolsModel {

	@IQColumn(autoIncrement = true, primaryKey = true)
	public int id;

	@IQColumn
	public boolean a;

	@IQColumn
	public boolean b;

	public MultipleBoolsModel() {
	}

	public MultipleBoolsModel(boolean a, boolean b) {
		this.a = a;
		this.b = b;
	}

	public static List<MultipleBoolsModel> getList() {
		return Arrays.asList(new MultipleBoolsModel(true, true), new MultipleBoolsModel(true, false),
				new MultipleBoolsModel(true, false), new MultipleBoolsModel(false, false));
	}
}