package org.esmartpoint.dbutil;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.esmartpoint.dbutil.db.metadata.FieldInfo;

public class NamedParameterStatement {
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NamedParameterStatement.class);
    /** The statement this object is wrapping. */
    private final PreparedStatement statement;

    /** Maps parameter names to arrays of ints which are the parameter indices. */
    private final Map<Object, Object> indexMap;
    
    Integer generatedKey = null;

    /**
     * Creates a NamedParameterStatement.  Wraps a call to
     * c.{@link Connection#prepareStatement(java.lang.String) prepareStatement}.
     * @param connection the database connection
     * @param query      the parameterized query
     * @throws SQLException if the statement could not be created
     */
    public NamedParameterStatement(Connection connection, String query) throws SQLException {
        indexMap = new Record();
        String parsedQuery = parse(query, indexMap);
		logger.debug("creatingQuery = " + query);
        String command = query.trim().toUpperCase();
		if (!command.startsWith("SELECT"))
			statement = connection.prepareStatement(parsedQuery, Statement.RETURN_GENERATED_KEYS);
		else
			statement = connection.prepareStatement(parsedQuery);
    }

    /**
     * Parses a query with named parameters.  The parameter-index mappings are put into the map, and the
     * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This method is non-private so JUnit code can
     * test it.
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    @SuppressWarnings("unchecked")
	static final String parse(String query, Map<Object, Object> paramMap) {
        // I was originally using regular expressions, but they didn't work well for ignoring
        // parameter-like strings inside quotes.
        int length=query.length();
        StringBuilder parsedQuery=new StringBuilder(length);
        boolean inSingleQuote=false;
        boolean inDoubleQuote=false;
        int index=1;

        for(int i=0;i<length;i++) {
            char c=query.charAt(i);
            if(inSingleQuote) {
                if(c=='\'') {
                    inSingleQuote=false;
                }
            } else if(inDoubleQuote) {
                if(c=='"') {
                    inDoubleQuote=false;
                }
            } else {
                if(c=='\'') {
                    inSingleQuote=true;
                } else if(c=='"') {
                    inDoubleQuote=true;
                } else if(c==':' && i+1<length &&
                        Character.isJavaIdentifierStart(query.charAt(i+1))) {
                    int j=i+2;
                    while(j<length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name=query.substring(i+1,j);
                    c='?'; // replace the parameter with a question mark
                    i+=name.length(); // skip past the end if the parameter
                    
                    List<Integer> indexList;
                    if (paramMap.containsKey(name)) {
                    	indexList=(List<Integer>)paramMap.get(name);
                    } else {
                    	indexList=new LinkedList<Integer>();
                    	paramMap.put(name, indexList);
                    }
                    indexList.add(new Integer(index));
                    index++;
                }
            }
            parsedQuery.append(c);
        }

        // replace the lists of Integer objects with arrays of ints
        for(Iterator<?> itr=paramMap.entrySet().iterator(); itr.hasNext();) {
            @SuppressWarnings("rawtypes")
			Map.Entry entry=(Map.Entry)itr.next();
            List<?> list=(List<?>)entry.getValue();
            int[] indexes=new int[list.size()];
            int i=0;
            for(Iterator<?> itr2=list.iterator(); itr2.hasNext();) {
                Integer x=(Integer)itr2.next();
                indexes[i++]=x.intValue();
            }
            entry.setValue(indexes);
        }

        return parsedQuery.toString();
    }


    /**
     * Returns the indexes for a parameter.
     * @param name parameter name
     * @return parameter indexes
     * @throws IllegalArgumentException if the parameter does not exist
     */
    private int[] getIndexes(String name) {
        int[] indexes=(int[])indexMap.get(name);
        if(indexes==null) {
            throw new IllegalArgumentException("Parameter not found: "+name);
        }
        return indexes;
    }


    /**
     * Sets a parameter.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, java.lang.Object)
     */
    public NamedParameterStatement setParameter(String name, Object value) throws SQLException {
        int[] indexes=getIndexes(name);
        for(int i=0; i < indexes.length; i++) {
        	if (value instanceof java.util.Date) {
        		statement.setTimestamp(indexes[i], new Timestamp(((java.util.Date)value).getTime()));
        	} else
        		statement.setObject(indexes[i], value);
        }
        return this;
    }

    /**
     * Returns the underlying statement.
     * @return the statement
     */
    public PreparedStatement getStatement() {
        return statement;
    }


    /**
     * Executes the statement.
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
    	try {
	    	boolean res = statement.execute();
	    	statement.close();
	        return res;
		} finally {
			Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
		}
    }


    /**
     * Executes the statement, which must be a query.
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
    	try {
	    	ResultSet res = statement.executeQuery();
	    	statement.close();
	        return res;
    	} finally {
    		Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
    	}
    }

    /**
     * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE statement;
     * or an SQL statement that returns nothing, such as a DDL statement.
     * @return number of rows affected
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
    	try {
    		int res = statement.executeUpdate();
    		ResultSet key = statement.getGeneratedKeys();
    		if (key.next()) {
    			generatedKey = key.getInt(1);
    			key.close();
    		}
    		key.close();
        	statement.close();
            return res;
    	} finally {
    		Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
    	}
    }

    /**
     * Closes the statement.
     * @throws SQLException if an error occurred
     * @see Statement#close()
     */
    public void close() throws SQLException {
        statement.close();
    }


    /**
     * Adds the current set of parameters as a batch entry.
     * @throws SQLException if something went wrong
     */
    public void addBatch() throws SQLException {
        statement.addBatch();
    }

    /**
     * Executes all of the batched statements.
     * 
     * See {@link Statement#executeBatch()} for details.
     * @return update counts for each statement
     * @throws SQLException if something went wrong
     */
    public int[] executeBatch() throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
    	try {
    		return statement.executeBatch();
    	} finally {
    		Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
    	}
    }
    
    public Integer getGeneratedKey() throws SQLException {
		return generatedKey;
    }
    
    public List<Record> list() throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
    	try {
	    	ResultSet rs = statement.executeQuery();
	    	List<Record> res = new ArrayList<Record>();
	    	
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();
			 
			while (rs.next()) {
				Record row = new Record();
				
				for (int i = 0; i < cols; i++ ) {
					Object o = rs.getObject(i + 1);
					if (o instanceof byte[])
						try {
							row.put(rsmd.getColumnLabel(i + 1), new String((byte[])o, "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							throw new SQLException(e);
						}
					else
						row.put(rsmd.getColumnLabel(i + 1), rs.getObject(i + 1));
				}
				res.add(row.mapToCamelCase());
			}
			rs.close();
	    	statement.close();
			return res;
    	} finally {
    		Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
    	}
    }
    
    public HashMap<String, FieldInfo> metaData() throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
    	try {
	    	ResultSet rs = statement.executeQuery();
	    	
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();
			
			HashMap<String, FieldInfo> md = new HashMap<String, FieldInfo>();

			for (int i = 0; i < cols; i++) {
				FieldInfo hmCol = new FieldInfo();
				String colName = rsmd.getColumnName(i);
				hmCol.setName(colName);
				hmCol.setPropertyName(Record.mapToCamelCase(colName));
				hmCol.setType(rsmd.getColumnType(i));
				hmCol.setTypeName(rsmd.getColumnTypeName(i));
				hmCol.setRemarks(rsmd.getColumnLabel(i));
				//hmCol.setDefaultValue(rsmd.getString("COLUMN_DEF"));
				hmCol.setSize(rsmd.getColumnDisplaySize(i));
				hmCol.setDecimals(rsmd.getPrecision(i));
				hmCol.setNullable(rsmd.isNullable(i) == ResultSetMetaData.columnNullable);
				md.put(colName, hmCol);
			}
			
	    	statement.close();
			return md;
    	} finally {
    		Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
    	}
    }
    
    public Record uniqueResult() throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
    	try {
	    	ResultSet rs = statement.executeQuery();
	    	
			if (!rs.next()) {
				rs.close();
		    	statement.close();
				return null;
			}
			
			Record row = new Record();
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();
			
			for (int i = 0; i < cols; i++ ) 
				row.put(rsmd.getColumnLabel(i + 1), rs.getObject(i + 1));
				
			rs.close();
	    	statement.close();
			return row.mapToCamelCase();
    	} finally {
    		Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
    	}
    }
    
    public Object singleElement() throws SQLException {
    	Cronometro.start("es.afu.util.NamedParameterStatement.DBQUERY");
    	try {
    		ResultSet rs = statement.executeQuery();
			if (!rs.next()) { 
				rs.close();
				return null;
			}
			Object res = rs.getObject(1);
			rs.close(); 
	    	statement.close();
			return res;
	    } finally {
			Cronometro.stop("es.afu.util.NamedParameterStatement.DBQUERY");
		}
    }
    
    public Number scalar() throws SQLException {
		return (Number)singleElement();
    }
}
