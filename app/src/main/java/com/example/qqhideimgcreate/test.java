package com.example.qqhideimgcreate;

import java.io.IOException;
import java.io.InputStream;

public class test {
    private void function(InputStream inputStream) throws IOException {
        byte[] tmp = new byte[1024];
        int length = -1;
        while ((length = inputStream.read(tmp))>0){

        }

    }
}
