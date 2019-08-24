package com.qingshangzuo.paint;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.CpuUsageInfo;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity {

    private Button btn_resume, btn_xi, btn_cu, btn_color, btn_clear, btn_autoclip, btn_clip, btn_extend, btn_import, btn_output, btn_html;  //清空 保存 细 粗 颜色 橡皮擦 自动裁剪 裁剪 延长 base64 导入 base64导出 html->canvas
    private ImageView iv_canvas;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    private int color[] ={ Color.RED, Color.GREEN, Color.BLUE, Color.BLACK };
    private int i = 1;
    private int Mode = 0; // 0 铅笔  1 橡皮擦
    private Paint eraserPaint = new Paint();
    private static final int CROP_CODE = 3;//剪切裁剪
    private final static int MY_PERMISSIONS_REQUEST_RECORD_STORAGE = 1;
    private final static int MY_PERMISSIONS_REQUEST_RECORD_STORAGE1 = 2;
    private List<Float> xlist = new ArrayList<>(); // x 坐标集合
    private List<Float> ylist = new ArrayList<>();  // y 坐标集合

    private Bitmap bmp;
    private int screenWidth;
    private int screenHeight;
    private WebView btn_webview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;


        // 初始化一个画笔，笔触宽度为3,铅笔,颜色为红色
        paint = new Paint();
        paint.setStrokeWidth(3);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setDither(true);

        // 初始化控件
        iv_canvas = findViewById(R.id.iv_canvas);
        btn_webview = findViewById(R.id.btn_webview);
        btn_resume = findViewById(R.id.btn_resume);
        btn_xi = findViewById(R.id.btn_xi);
        btn_cu = findViewById(R.id.btn_cu);
        btn_color = findViewById(R.id.btn_color);
        btn_clear = findViewById(R.id.btn_clear);
        btn_autoclip = findViewById(R.id.btn_autoclip);
        btn_clip = findViewById(R.id.btn_clip);
        btn_extend = findViewById(R.id.btn_extend);
        btn_import = findViewById(R.id.btn_import);
        btn_output = findViewById(R.id.btn_output);
        btn_html = findViewById(R.id.btn_html);

        //添加点击监听
        btn_resume.setOnClickListener(click);
        btn_clear.setOnClickListener(click);
        btn_color.setOnClickListener(click);
        btn_cu.setOnClickListener(click);
        btn_xi.setOnClickListener(click);
        btn_html.setOnClickListener(click);
        btn_output.setOnClickListener(click);
        btn_import.setOnClickListener(click);
        btn_extend.setOnClickListener(click);
        btn_clip.setOnClickListener(click);
        btn_autoclip.setOnClickListener(click);


        iv_canvas.setOnTouchListener(touch);

    }

    private View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_resume:
                    resumeCanvas();
                    break;
                case R.id.btn_cu:
                    paint.setStrokeWidth(10);
                    Mode = 0;
                    break;
                case R.id.btn_xi:
                    paint.setStrokeWidth(3);
                    Mode = 0;
                    break;
                case R.id.btn_color:
                    if (Mode == 0) {
                        Log.i("i的值", String.valueOf(i));
                        if (i == 4) {
                            i = 0;
                        }
                        paint.setColor(color[i]);
                        ++i;
                    }
                    break;
                case R.id.btn_clear:
                    Mode = 1;
                    eraserPaint.setAlpha(0);
                    eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));  //设置图像的混合模式
                    eraserPaint.setAntiAlias(true);  //抗锯齿
                    eraserPaint.setDither(true);   //设置防抖动
                    eraserPaint.setStrokeCap(Paint.Cap.ROUND);   //设置线帽 圆形
                    eraserPaint.setStyle(Paint.Style.STROKE);   //设置橡皮擦样式  空心
                    eraserPaint.setStrokeJoin(Paint.Join.ROUND);
                    eraserPaint.setStrokeWidth(15); //设置橡皮擦宽度
//                    eraserPaint.setColor(0xFF00FF00);
//                    eraserPaint.setColor(Color.WHITE);
                    break;
                case R.id.btn_autoclip:
                    /**
                     * 第 1 步: 检查是否有相应的权限
                     */
                    boolean isAllGranted1 = checkPermissionAllGranted(
                            new String[]{
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }
                    );
                    if (!isAllGranted1) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_RECORD_STORAGE1);
                    } else {
                        if (xlist.size() != 0 && ylist.size() != 0) {
//                            removeDuplicate(xlist);
//                            removeDuplicate(ylist);
                            //x 坐标最小值 最大值
                            float max_x = Float.valueOf(Collections.max(xlist));
                            float min_x = Float.valueOf(Collections.min(xlist));
                            //y 坐标最小值 最大值
                            float max_y = Float.valueOf(Collections.max(ylist));
                            float min_y = Float.valueOf(Collections.min(ylist));
//                            canvas.clipRect(min_x, min_y, max_x, max_y);
                            float tip_x = max_x - min_x;
                            float tip_y = max_y - min_y;
                            bmp = Bitmap.createBitmap((int) (tip_x + 10),
                                    (int) (tip_y + 10), Bitmap.Config.ARGB_8888);
                            Canvas canvas1 = new Canvas(bmp);
                            canvas1.drawColor(Color.WHITE);
                            Rect mSrcRect = new Rect((int) min_x - 5, (int) min_y - 5, (int) max_x + 5, (int) max_y + 5);
                            Rect mDestRect = new Rect(0, 0, (int) (tip_x + 10), (int) (tip_y + 10));
                            canvas1.drawBitmap(baseBitmap, mSrcRect, mDestRect, null);  //把baseBitmap中需要的绘制区域mSrcRect 截取出来 放入 新创建的（宽高为需要截取的矩形区域）bmp中的mDestRect位置 在利用bmp的canvas绘制出来

                            //canvas1.save(Canvas.ALL_SAVE_FLAG);  这样会出现包错
                            canvas1.save();
                            canvas1.restore();
                            saveBitmap1();
                        }
//                        saveBitmap();
                    }

                    break;
                case R.id.btn_clip:
                    /**
                     * 第 1 步: 检查是否有相应的权限
                     */
                    boolean isAllGranted = checkPermissionAllGranted(
                            new String[]{
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }
                    );
                    if (!isAllGranted) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_RECORD_STORAGE);
                    } else {
                        saveBitmap();
                    }

                    break;
                case R.id.btn_extend:


                    break;
                case R.id.btn_import:

                    Intent intent = new Intent(MainActivity.this,PhotoAty.class);
                    startActivity(intent);

                    break;
                case R.id.btn_output:


                    break;
                case R.id.btn_html:
                    btn_webview.setVisibility(View.VISIBLE);
                    StringBuilder sb = new StringBuilder();
                    // 拼接一段HTML代码
                    sb.append("<html>");
                    sb.append("<head>");
                    sb.append("<title> 欢迎您 </title>");
                    sb.append("</head>");
                    sb.append("<body>");
                    sb.append("<h2> 欢迎您访问<a href=\"http://www.github.com\">"
                            + "Github</a></h2>");
                    sb.append("</body>");
                    sb.append("</html>");
                    //  加载、并显示HTML代码
                    btn_webview.loadDataWithBaseURL(null, sb.toString(), "text/html", "utf-8", null);

                    break;
                default:
                    break;
            }
        }
    };

    private Uri imageUri;

    /**
     * 保存图片到SD卡上
     */

    private void saveBitmap() {
        try {
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Canvas");        // Create the storage directory if it does not exist
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs();
            }
            File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".png");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                baseBitmap.compress(Bitmap.CompressFormat.PNG, 50, fos);  //压缩 写入

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= 24) {
                imageUri = FileProvider.getUriForFile(MainActivity.this, "com.qingshangzuo.paint.fileprovider", file);
            } else {
                imageUri = Uri.fromFile(file);
            }
//        File file = new File(Environment.getExternalStorageDirectory(),
//                System.currentTimeMillis() + ".png");
//        imageUri = Uri.fromFile(file);
            Intent intent = new Intent("com.android.camera.action.CROP");
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //可以选择图片类型，如果是*表明所有类型的图片
            intent.setDataAndType(imageUri, "image/*");
            // 下面这个crop = true是设置在开启的Intent中设置显示的VIEW可裁剪
            intent.putExtra("crop", "true");
            // aspectX aspectY 是宽高的比例，这里设置的是正方形（长宽比为1:1）
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            // outputX outputY 是裁剪图片宽高
//            intent.putExtra("outputX", 500);
//            intent.putExtra("outputY", 500);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            //是否将数据保留在Bitmap中返回,true返回bitmap，false返回uri
            intent.putExtra("return-data", false);
            //裁剪后的图片Uri路径，uritempFile为Uri类变量
//            uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
            startActivityForResult(intent, CROP_CODE);


//            Toast.makeText(MainActivity.this, "保存图片成功", Toast.LENGTH_SHORT).show();
        } catch (
                Exception e)

        {
            Toast.makeText(MainActivity.this, "保存图片失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_STORAGE) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码
                saveBitmap();

            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                android.widget.Toast.makeText(MainActivity.this, "Permission Denied", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_STORAGE1) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码
                if (xlist.size() != 0 && ylist.size() != 0) {
                    removeDuplicate(xlist);
                    removeDuplicate(ylist);
                    //x 坐标最小值 最大值
                    float max_x = Float.valueOf(Collections.max(xlist));
                    float min_x = Float.valueOf(Collections.min(xlist));
                    //y 坐标最小值 最大值
                    float max_y = Float.valueOf(Collections.max(ylist));
                    float min_y = Float.valueOf(Collections.min(ylist));
//                            canvas.clipRect(min_x, min_y, max_x, max_y);
                    float tip_x = (max_x + Float.valueOf(5)) - min_x;
                    float tip_y = (max_y + Float.valueOf(5)) - min_y;
                    bmp = Bitmap.createBitmap((int) (max_x + 5),
                            (int) (max_y + 5), Bitmap.Config.ARGB_8888);
                    Canvas canvas1 = new Canvas(bmp);
                    canvas1.drawColor(Color.WHITE);

                    canvas1.drawBitmap(baseBitmap, 0, 0, null);

                    canvas1.save();
                    canvas1.restore();
                    saveBitmap1();
                }

            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                android.widget.Toast.makeText(MainActivity.this, "Permission Denied", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }

    }


    private Uri imageUri1;

    /**
     * 保存图片到SD卡上
     */
    protected  void saveBitmap1() {
        try {
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Canvas");        // Create the storage directory if it does not exist
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs();
            }
            File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".png");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 50, fos);  //压缩 写入

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= 24) {
                imageUri1 = FileProvider.getUriForFile(MainActivity.this, "com.qingshangzuo.paint.fileprovider", file);
            } else {
                imageUri1 = Uri.fromFile(file);
            }

            if (imageUri1 != null) {
                Intent intent = new Intent(MainActivity.this, PhotoAty.class);
                intent.setDataAndType(imageUri1, "url");
                startActivity(intent);
            }
        } catch (Exception e)

        {
            Toast.makeText(MainActivity.this, "保存图片失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }


    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /**
     * 清除画板
     */
    private void resumeCanvas() {
        // 手动清除画板的绘图，重新创建一个画板
        if (baseBitmap != null) {
            baseBitmap = Bitmap.createBitmap(iv_canvas.getWidth(),
                    iv_canvas.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
//            canvas.drawColor(Color.WHITE);
            iv_canvas.setImageBitmap(baseBitmap);
            xlist.clear();
            ylist.clear();
            btn_webview.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "清除画板成功，可以重新开始绘图", Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnTouchListener touch = new View.OnTouchListener() {
        // 定义手指开始触摸的坐标
        float startX;
        float startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                // 用户按下动作
                case MotionEvent.ACTION_DOWN:
                    // 第一次绘图初始化内存图片，指定背景为白色
                    if (baseBitmap == null) {
                        baseBitmap = Bitmap.createBitmap(iv_canvas.getWidth(),
                                iv_canvas.getHeight(), Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(baseBitmap);
//                        canvas.drawColor(Color.WHITE);

                    }
                    // 记录开始触摸的点的坐标
                    startX = event.getX();
                    startY = event.getY();
                    xlist.add((startX));
                    ylist.add((startY));
                    break;
                // 用户手指在屏幕上移动的动作
                case MotionEvent.ACTION_MOVE:
                    // 记录移动位置的点的坐标
                    float stopX = event.getX();
                    float stopY = event.getY();
                    xlist.add((stopX));
                    ylist.add((stopY));
                    //根据两点坐标，绘制连线
                    if (Mode == 0) {
                        canvas.drawLine(startX, startY, stopX, stopY, paint);
                        canvas.save();
                        canvas.restore();
                    } else if (Mode == 1) {
                        canvas.drawLine(startX, startY, stopX, stopY, eraserPaint);
                    }
                    // 更新开始点的位置
                    startX = event.getX();
                    startY = event.getY();
                    xlist.add((startX));
                    ylist.add((startY));
                    // 把图片展示到ImageView中
                    iv_canvas.setImageBitmap(baseBitmap);
                    break;
                case MotionEvent.ACTION_UP:

                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Activity.RESULT_OK && data != null){
            switch (requestCode){
                case CROP_CODE:
                    //获取到裁剪后的图片的Uri进行处理

                    if (imageUri != null) {
                        Intent intent = new Intent(MainActivity.this, PhotoAty.class);
                        intent.setDataAndType(imageUri, "url");
                        startActivity(intent);
                    }
                    break;
            }
        }
    }

    public static List removeDuplicate(List list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).equals(list.get(i))) {
                    list.remove(j);
                }
            }
        }
        return list;
    }

}
