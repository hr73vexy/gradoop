package org.gradoop.flink.io.impl.rdbms.functions;

import java.util.List;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.io.impl.rdbms.metadata.TableToEdge;
import org.gradoop.flink.io.impl.rdbms.tuples.IdKeyTuple;

public class VertexToIdKeyTuple extends RichFlatMapFunction<TableToEdge, Tuple3<String,GradoopId,String>> {
	List<Vertex> vertices;

	@Override
	public void flatMap(TableToEdge table, Collector<Tuple3<String,GradoopId,String>> out) throws Exception {
		String label = table.getStartAttribute().f0;
		GradoopId id;
		String key;
		
		for (Vertex v : vertices) {
			if (v.getLabel().equals(table.getstartTableName())) {
				id = v.getId();
				key = v.getProperties().get(table.getStartAttribute().f0).toString();
				out.collect(new Tuple3<>(label, id, key));
			}
			if(v.getLabel().equals(table.getendTableName())){
				id = v.getId();
				key = v.getProperties().get(table.getEndAttribute().f0).toString();
				out.collect(new Tuple3<>(label, id, key));
			}
		}
	}

	public void open(Configuration parameters) throws Exception {
		this.vertices = getRuntimeContext().getBroadcastVariable("vertices");
	}
}