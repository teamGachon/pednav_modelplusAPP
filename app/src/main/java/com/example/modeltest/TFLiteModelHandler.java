package com.example.modeltest;



import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModelHandler {
    private Interpreter interpreter;

    public TFLiteModelHandler(Context context, String modelFileName) throws IOException {
        interpreter = new Interpreter(loadModelFile(context, modelFileName));
    }

    private MappedByteBuffer loadModelFile(Context context, String modelFileName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    public float predict(float[][][][] inputData) {
        float[][] outputData = new float[1][1];
        interpreter.run(inputData, outputData);
        return outputData[0][0];
    }

    public void close() {
        interpreter.close();
    }
}