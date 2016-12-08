package hu.elte.computernetworks.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.elte.computernetworks.model.Network;

import java.io.File;
import java.io.IOException;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class Load {
    //region fields
    private ObjectMapper mapper;
    private String fileName;
    //endregion

    //region constructor
    public Load(String fileName) {
        this.fileName = fileName;
        this.mapper = new ObjectMapper();
    }

    public Load(){
        this("C:\\Users\\andris.DESKTOP-BQJ4DSD\\Desktop\\test.json");
    }
    //endregion

    public Network read() throws IOException {
        Network network = mapper.readValue(new File(fileName),Network.class);
        return network;
    }
}
