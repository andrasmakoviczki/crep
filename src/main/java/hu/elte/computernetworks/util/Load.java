package hu.elte.computernetworks.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.elte.computernetworks.Network;
import hu.elte.computernetworks.model.Cluster;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Andras Makoviczki on 2016. 11. 27..
 */
public class Load {
    private ObjectMapper mapper;
    private String fileName;

    public Load(String fileName) {
        this.fileName = fileName;
        this.mapper = new ObjectMapper();
    }

    public Load(){
        this("C:\\Users\\andris.DESKTOP-BQJ4DSD\\Desktop\\test.json");
    }

    public Network read() throws IOException {
        Network network = mapper.readValue(new File(fileName),Network.class);
        return network;
    }
}
