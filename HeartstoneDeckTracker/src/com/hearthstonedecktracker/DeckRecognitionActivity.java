package com.hearthstonedecktracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Activity to recognize decks in pictures taken.
 * A lot of the code here is from: http://developer.android.com/training/camera/photobasics.html
 * and: https://github.com/GautamGupta/Simple-Android-OCR/blob/master/src/com/datumdroid/android/ocr/simple/SimpleAndroidOCRActivity.java#L76
 * OCR file is from: http://code.google.com/p/tesseract-ocr/downloads/list
 */
public class DeckRecognitionActivity extends Activity {

	private static final int REQUEST_TAKE_PHOTO = 1;
	private static final String OCR_DATA_ASSETS_PATH = "OCR/";
	private static final String OCR_DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/OCR/";
	private static final String LANG = "eng";

	private String currentPhotoPath;
	private LinearLayout mainLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = 
				(LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainLayout = (LinearLayout) inflater.inflate(R.layout.deck_recognition_activity, null);
		setContentView(mainLayout);

		saveOCRFileIfNotExists();
		dispatchTakePictureIntent();
	}

	private void saveOCRFileIfNotExists() {
		String[] paths = {OCR_DATA_PATH, OCR_DATA_PATH + "tessdata/"};

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

		if (!(new File(OCR_DATA_PATH + "tessdata/" + LANG + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open(OCR_DATA_ASSETS_PATH + LANG + ".traineddata");
				OutputStream out = new FileOutputStream(OCR_DATA_PATH + "tessdata/" + LANG + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

			} catch (IOException e) {
				Log.e("IOException", "Was unable to copy " + LANG + " traineddata " + e.toString());
			}
		}
	}

	/**
	 * Starts a camera activity, calls onActivityResult when done
	 */
	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	/**
	 * Method called when the camera has taken a picture
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO) {
			if (resultCode == RESULT_OK) {
				onPhotoTaken();
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
				//TODO: Add an option to return to the rest of the app after cancelling
			} else {
				// Image capture failed, advise user
			}
		}
	}

	/**
	 * What happens when a photo is taken
	 */
	private void onPhotoTaken() {
		Bitmap image = BitmapFactory.decodeFile(currentPhotoPath);
		ImageView resultImage = (ImageView) findViewById(R.id.deck_recognition_result_image);

		try {
			image = rotateHorizontally(currentPhotoPath, image);
		} catch (IOException e) {
			//Shouldn't happen, as we just read the file
		}

		resultImage.setImageBitmap(image);
		
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(OCR_DATA_PATH, LANG);
		baseApi.setImage(image);

		String recognizedText = baseApi.getUTF8Text();
		Log.e("Result:", recognizedText);

		baseApi.end();
	}

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(
				imageFileName,
				".jpg",
				storageDir
				);

		currentPhotoPath = image.getAbsolutePath();
		return image;
	}

	/**
	 * Rotates a bitmap so that text is horizontal
	 * @param path - The path to the image on the sd card
	 * @param bitmap - The image itself
	 * @return - The image rotated properly
	 * @throws IOException - If the file cannot be found
	 */
	private Bitmap rotateHorizontally(String path, Bitmap bitmap) throws IOException {
		ExifInterface exif = new ExifInterface(path);
		int exifOrientation = exif.getAttributeInt(
				ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);

		int rotate = 0;

		switch (exifOrientation) {
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotate = 90;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotate = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotate = 270;
			break;
		}

		if (rotate != 0) {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();

			// Setting pre rotate
			Matrix mtx = new Matrix();
			mtx.preRotate(rotate);

			// Rotating Bitmap & convert to ARGB_8888, required by tess
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
		}
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		return bitmap;
	}

}
