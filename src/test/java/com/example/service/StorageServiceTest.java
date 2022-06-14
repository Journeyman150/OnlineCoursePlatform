package com.example.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StorageServiceTest {

    private static String path1 = "E:\\some_directory_that_exists";
    private static String absPath1 = path1 + "\\some_file.txt";

    private static String path2 = "E:\\some_directory_that_not_exists";
    private static String absPath2 = path2 + "\\some_file.txt";

    private static byte[] content = new byte[]{1,2,3,4,5,6};
    private static MultipartFile multipartFile = new MockMultipartFile("mockFile", content);

    @Autowired
    private StorageService storageService;

    @Before
    public void setup() throws IOException {
        File path = new File(path1);
        if (!path.exists()) {
            path.mkdir();
        }
        File file = new File(path1 + "\\some_file.txt");
        file.createNewFile();
    }

    @After
    public void clean() {
        File file = new File(path2);
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    entry.delete();
                }
            }
            file.delete();
        }
    }

    @Test
    public void store() throws IOException {
        storageService.store(multipartFile, path1, absPath1);
        byte[] bArr1 = read(absPath1);
        assertArrayEquals(content, bArr1);

        storageService.store(multipartFile, path2, absPath2);
        assertTrue(Files.isDirectory(Paths.get(path2)));
        byte[] bArr2 = read(absPath2);
        assertArrayEquals(content, bArr2);
    }

    public byte[] read(String path) throws IOException {
        File file = new File(path);
        byte[] bArr = new byte[(int)file.length()];
        FileReader fileReader = new FileReader(file);
        int b = fileReader.read();
        for (int i = 0; b > 0; i++) {
            bArr[i] = (byte)b;
            b = fileReader.read();
        }
        return bArr;
    }

    @Test
    public void load() {
        File file = storageService.load(absPath1);
        assertEquals(file, new File(absPath1));
    }

    @Test
    public void delete() {
        storageService.delete(absPath1);
        assertFalse(Files.exists(Paths.get(absPath1)));
    }
}