package hu.elte.computernetworks.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import hu.elte.computernetworks.model.Network;
import hu.elte.computernetworks.model.Cluster;

import java.io.File;
import java.io.IOException;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class Save {
    //region fields
    private ObjectMapper mapper;
    private String fileName;
    //endregion

    //region constructor
    public Save(String fileName) {
        this.mapper = new ObjectMapper();
        this.fileName = fileName;
    }

    public Save() {
        this("C:\\Users\\andris.DESKTOP-BQJ4DSD\\Desktop\\test.json");
    }
    //endregion

    public void write(Network network) throws IOException {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), network);
    }

}
