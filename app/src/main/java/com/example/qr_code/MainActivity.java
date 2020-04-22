package com.example.qr_code;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView image; // вывод изображения
    Button genBt, // кнопка генерации
            scanBt; // кнопка сканирования
    EditText inputET; // поле ввода
    Bitmap bitmap;
    TextView outputResTxt; // поле вывода
    public static final int QRcodeSize = 350; // размер кода
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = findViewById(R.id.image);
        inputET = findViewById(R.id.inputET);
        genBt = findViewById(R.id.genBt);
        scanBt = findViewById(R.id.scanBt);
        outputResTxt = findViewById(R.id.outputResText);

        genBt.setOnClickListener(genBtClick());
        image.setOnClickListener(imageOnClick());
        scanBt.setOnClickListener(scanBtOnClick());
    }

    private View.OnClickListener scanBtOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Сканирование");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            }
        };
    }

    private View.OnClickListener imageOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable draw = (BitmapDrawable) ((ImageView) view).getDrawable();
                Bitmap saveBmp = draw.getBitmap();
                FileOutputStream outputStream = null;
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + "/QR-code");
                dir.mkdir();
                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);
                try {
                    outputStream = new FileOutputStream(outFile);
                    saveBmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    Toast.makeText(MainActivity.this,
                            "Сохранено",
                            Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    private View.OnClickListener genBtClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputET.getText().toString().isEmpty()){
                    String resTxtValue = inputET.getText().toString();
                    bitmap = TextToImageEncode(resTxtValue);
                    image.setImageBitmap(bitmap);
                } else {
                    inputET.requestFocus();
                    Toast.makeText(MainActivity.this,
                            "Сначала введите текст",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Bitmap TextToImageEncode(String resTxtValue) {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    resTxtValue,
                    BarcodeFormat.QR_CODE,
                    QRcodeSize,
                    QRcodeSize,
                    null
            );
        } catch (WriterException e) {
            return null;
        }
        int bitMatrixCol = bitMatrix.getWidth();
        int bitMatrixRow = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixCol*bitMatrixRow];

        for (int i = 0; i < bitMatrixRow; i++){
            int offset = i * bitMatrixCol;
            for (int j = 0; j < bitMatrixCol; j++){
                pixels[offset+j] = bitMatrix.get(j, i) ?
                        getResources().getColor(R.color.blackColor) :
                        getResources().getColor(R.color.whiteColor);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(bitMatrixCol, bitMatrixRow,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, QRcodeSize, 0, 0,
                bitMatrixCol, bitMatrixRow);
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents() == null){;
                Log.i("Scan", "Сканер не передал информацию");
            } else {
                Log.i("Scan", "Просканировано");
                String resultStr = "Результат: " + result.getContents();
                outputResTxt.setText(resultStr);
                Toast.makeText(this, resultStr, Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
