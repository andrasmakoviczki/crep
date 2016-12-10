package hu.elte.computernetworks.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.elte.computernetworks.model.Network;

import java.io.File;
import java.io.IOException;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class Save {
    //region fields
    private final ObjectMapper mapper;
    private final String fileName;
    //endregion

    //region constructor
    public Save(String fileName) {
        this.mapper = new ObjectMapper();
        this.fileName = fileName;
    }
    //endregion

    //region util
    public void write(Network network) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), network);
    }
    //endregion
}
