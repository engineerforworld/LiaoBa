package com.wwl.liaoba.message;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.imlib.NativeClient;
import io.rong.imlib.model.Message;
import io.rong.message.MessageHandler;
import io.rong.message.utils.BitmapUtil;

/**
 * <p>Description:
 * <p>Company：
 * <p>Email:bjxm2013@163.com
 * <p>Created by Devin Sun on 2018/3/27.
 */

public class BurnAfterReadMessageHandler extends MessageHandler<BurnAfterReadMessage> {
    private static final String TAG = "ImageMessageHandler";
    private static int COMPRESSED_SIZE = 960;
    private static int COMPRESSED_QUALITY = 85;
    private static int THUMB_COMPRESSED_SIZE = 240;
    private static int THUMB_COMPRESSED_MIN_SIZE = 100;
    private static int THUMB_COMPRESSED_QUALITY = 30;
    private static final String IMAGE_LOCAL_PATH = "/image/local/";
    private static final String IMAGE_THUMBNAIL_PATH = "/image/thumbnail/";

    public BurnAfterReadMessageHandler(Context context) {
        super(context);
    }



    public void decodeMessage(Message message, BurnAfterReadMessage model) {
        Uri uri = obtainImageUri(this.getContext());
        String name = message.getMessageId() + ".jpg";
        if(message.getMessageId() == 0) {
            name = message.getSentTime() + ".jpg";
        }

        String thumb = uri.toString() + "/image/thumbnail/";
        String local = uri.toString() + "/image/local/";
        File localFile = new File(local + name);
        if(localFile.exists()) {
            model.setLocalUri(Uri.parse("file://" + local + name));
        }

        File thumbFile = new File(thumb + name);
        if(!TextUtils.isEmpty(model.getBase64()) && !thumbFile.exists()) {
            byte[] data = null;

            try {
                data = Base64.decode(model.getBase64(), 2);
            } catch (IllegalArgumentException var11) {
                RLog.e("BurnAfterReadMessageHandler", "afterDecodeMessage Not Base64 Content!");
                var11.printStackTrace();
            }

            if(!isImageFile(data)) {
                RLog.e("BurnAfterReadMessageHandler", "afterDecodeMessage Not Image File!");
                return;
            }

            FileUtils.byte2File(data, thumb, name);
        }

        model.setThumUri(Uri.parse("file://" + thumb + name));
        model.setBase64((String)null);
    }

    public void encodeMessage(Message message) {
        BurnAfterReadMessage model = (BurnAfterReadMessage)message.getContent();
        Uri uri = obtainImageUri(this.getContext());
        String name = message.getMessageId() + ".jpg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Resources resources = this.getContext().getResources();

        try {
            COMPRESSED_QUALITY = resources.getInteger(resources.getIdentifier("rc_image_quality", "integer", this.getContext().getPackageName()));
            COMPRESSED_SIZE = resources.getInteger(resources.getIdentifier("rc_image_size", "integer", this.getContext().getPackageName()));
        } catch (Resources.NotFoundException var12) {
            var12.printStackTrace();
        }

        if(model.getThumUri() != null && model.getThumUri().getScheme() != null && model.getThumUri().getScheme().equals("file")) {
            File file = new File(uri.toString() + "/image/thumbnail/" + name);
            byte[] data;
            if(file.exists()) {
                model.setThumUri(Uri.parse("file://" + uri.toString() + "/image/thumbnail/" + name));
                data = FileUtils.file2byte(file);
                if(data != null) {
                    model.setBase64(Base64.encodeToString(data, 2));
                }
            } else {
                try {
                    String thumbPath = model.getThumUri().toString().substring(5);
                    RLog.d("BurnAfterReadMessageHandler", "beforeEncodeMessage Thumbnail not save yet! " + thumbPath);
                    BitmapFactory.decodeFile(thumbPath, options);
                    if(options.outWidth <= THUMB_COMPRESSED_SIZE && options.outHeight <= THUMB_COMPRESSED_SIZE) {
                        File src = new File(thumbPath);
                        data = FileUtils.file2byte(src);
                        if(data != null) {
                            model.setBase64(Base64.encodeToString(data, 2));
                            String path = uri.toString() + "/image/thumbnail/";
                            if(FileUtils.copyFile(src, path, name) != null) {
                                model.setThumUri(Uri.parse("file://" + path + name));
                            }
                        }
                    } else {
                        Bitmap bitmap = BitmapUtil.getThumbBitmap(this.getContext(), model.getThumUri(), THUMB_COMPRESSED_SIZE, THUMB_COMPRESSED_MIN_SIZE);
                        if(bitmap != null) {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, THUMB_COMPRESSED_QUALITY, outputStream);
                            data = outputStream.toByteArray();
                            model.setBase64(Base64.encodeToString(data, 2));
                            outputStream.close();
                            FileUtils.byte2File(data, uri.toString() + "/image/thumbnail/", name);
                            model.setThumUri(Uri.parse("file://" + uri.toString() + "/image/thumbnail/" + name));
                            if(!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                        }
                    }
                } catch (IllegalArgumentException var14) {
                    var14.printStackTrace();
                    RLog.e("BurnAfterReadMessageHandler", "beforeEncodeMessage Not Base64 Content!");
                } catch (IOException var15) {
                    var15.printStackTrace();
                    RLog.e("BurnAfterReadMessageHandler", "beforeEncodeMessage IOException");
                }
            }
        }

        if(model.getLocalUri() != null && model.getLocalUri().getScheme() != null && model.getLocalUri().getScheme().equals("file")) {
            File file = new File(uri.toString() + "/image/local/" + name);
            if(file.exists()) {
                model.setLocalUri(Uri.parse("file://" + uri.toString() + "/image/local/" + name));
            } else {
                try {
                    String localPath = model.getLocalUri().toString().substring(5);
                    BitmapFactory.decodeFile(localPath, options);
                    if((options.outWidth > COMPRESSED_SIZE || options.outHeight > COMPRESSED_SIZE) && !model.isFull()) {
                        Bitmap bitmap = BitmapUtil.getResizedBitmap(this.getContext(), model.getLocalUri(), COMPRESSED_SIZE, COMPRESSED_SIZE);
                        if(bitmap != null) {
                            String dir = uri.toString() + "/image/local/";
                            file = new File(dir);
                            if(!file.exists()) {
                                file.mkdirs();
                            }

                            file = new File(dir + name);
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSED_QUALITY, bos);
                            bos.close();
                            model.setLocalUri(Uri.parse("file://" + dir + name));
                            if(!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                        }
                    } else if(FileUtils.copyFile(new File(localPath), uri.toString() + "/image/local/", name) != null) {
                        model.setLocalUri(Uri.parse("file://" + uri.toString() + "/image/local/" + name));
                    }
                } catch (IOException var13) {
                    var13.printStackTrace();
                    RLog.e("BurnAfterReadMessageHandler", "beforeEncodeMessage IOException");
                }
            }
        }

    }

    private static Uri obtainImageUri(Context context) {
        File file = context.getFilesDir();
        String path = file.getAbsolutePath();
        String userId = NativeClient.getInstance().getCurrentUserId();
        return Uri.parse(path + File.separator + userId);
    }

    private static boolean isImageFile(byte[] data) {
        if(data != null && data.length != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            return options.outWidth != -1;
        } else {
            return false;
        }
    }
}
