package com.example.modeltest;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModelLoader {
    private Interpreter interpreter;

    // 모델 로드 메서드
    public TFLiteModelLoader(Context context, String modelPath) throws IOException {
        this.interpreter = new Interpreter(loadModelFile(context, modelPath));
    }

    // Asset에서 모델 파일 읽기
    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Interpreter 반환
    public Interpreter getInterpreter() {
        return interpreter;
    }
}