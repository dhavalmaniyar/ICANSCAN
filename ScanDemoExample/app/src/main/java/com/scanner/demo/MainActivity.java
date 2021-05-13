package com.scanner.demo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    private Button scanButton;
    private FloatingActionButton cameraButton;
    private FloatingActionButton mediaButton;
    private ImageView scannedImageView;
    private Uri imageUri;
    private MenuView.ItemView createPdfMenuItemView;
    private Menu mainMenu;
    private TextView text;
    private TextView text2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        cameraButton = (FloatingActionButton) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = (FloatingActionButton) findViewById(R.id.mediaButton);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);
        createPdfMenuItemView = (MenuView.ItemView) findViewById(R.id.action_create_pdf);
        text=findViewById(R.id.myText);
        text2=findViewById(R.id.myText2);
    }

    private class ScanButtonClickListener implements View.OnClickListener {

        private int preference;

        public ScanButtonClickListener(int preference) {
            this.preference = preference;
        }

        public ScanButtonClickListener() {
        }

        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mainMenu.findItem(R.id.action_create_pdf).setVisible(true);
            text.setVisibility(View.INVISIBLE);
            text2.setVisibility(View.INVISIBLE);
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            String filePath = data.getExtras().getString("abcd");
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                scannedImageView.setImageBitmap(bitmap);
                imageUri = uri;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap convertByteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mainMenu = menu;
        MenuItem createPdfMenuItem = menu.findItem(R.id.action_create_pdf);
        createPdfMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_create_pdf) {
            try {
                saveAsPDF(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveAsPDF(Uri imageUri) throws IOException, DocumentException {
        File imageFile = new File(getRealPathFromURI(imageUri));

        Bitmap b = null;
        Image image = null;
        try {
            b = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            image = Image.getInstance(stream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document document = new Document();

        String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();

        PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "/" + imageFile.getName() + ".pdf")); //  Change pdf's name.

        document.open();

        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
        image.scalePercent(scaler);
        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

        document.add(image);
        document.close();
        Toast.makeText(getApplicationContext(), "PDF saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
// my code
//    private void createPDF() {
//        final File file = new File(uploadFolder, "AnswerSheet_" + queId + ".pdf");
//
//        final ProgressDialog dialog = ProgressDialog.show(this, "", "Generating PDF...");
//        dialog.show();
//        new Thread(() -> {
//            Bitmap bitmap;
//            PdfDocument document = new PdfDocument();
//            //  int height = 842;
//            //int width = 595;
//            int height = 1010;
//            int width = 714;
//            int reqH, reqW;
//            reqW = width;
//
//            for (int i = 0; i < array.size(); i++) {
//                //  bitmap = BitmapFactory.decodeFile(array.get(i));
//                bitmap = Utility.getCompressedBitmap(array.get(i), height, width);
//
//
//                reqH = width * bitmap.getHeight() / bitmap.getWidth();
//                Log.e("reqH", "=" + reqH);
//                if (reqH < height) {
//                    //  bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
//                } else {
//                    reqH = height;
//                    reqW = height * bitmap.getWidth() / bitmap.getHeight();
//                    Log.e("reqW", "=" + reqW);
//                    //   bitmap = Bitmap.createScaledBitmap(bitmap, reqW, reqH, true);
//                }
//                // Compress image by decreasing quality
//                // ByteArrayOutputStream out = new ByteArrayOutputStream();
//                //  bitmap.compress(Bitmap.CompressFormat.WEBP, 50, out);
//                //    bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
//                //bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);
//                //Create an A4 sized page 595 x 842 in Postscript points.
//                //PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
//                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(reqW, reqH, 1).create();
//                PdfDocument.Page page = document.startPage(pageInfo);
//                Canvas canvas = page.getCanvas();
//
//                Log.e("PDF", "pdf = " + bitmap.getWidth() + "x" + bitmap.getHeight());
//                canvas.drawBitmap(bitmap, 0, 0, null);
//
//                document.finishPage(page);
//            }
//
//            FileOutputStream fos;
//            try {
//                fos = new FileOutputStream(file);
//                document.writeTo(fos);
//                document.close();
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            runOnUiThread(() -> {
//                dismissDialog(dialog);
//
//            });
//        }).start();
//    }
}
