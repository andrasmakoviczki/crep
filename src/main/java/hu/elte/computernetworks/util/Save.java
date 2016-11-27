package hu.elte.computernetworks.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import hu.elte.computernetworks.Network;
import hu.elte.computernetworks.model.Cluster;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class Save {
    private ObjectMapper mapper;
    private String fileName;

    public Save(String fileName) {
        this.mapper = new ObjectMapper();
        this.fileName = fileName;
    }

    public Save() {
        this("C:\\Users\\andris.DESKTOP-BQJ4DSD\\Desktop\\test.json");
    }

    public void write(Network network) throws IOException {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), network);
    }

    private void Serialize(Cluster cluster, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeObjectFieldStart("clusters");
        jgen.writeObjectField("node", cluster.getId());
    }
}
