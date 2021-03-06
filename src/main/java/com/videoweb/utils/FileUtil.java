package com.videoweb.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class FileUtil extends HttpServlet {

    /**
     * 图片格式后缀
     */
    public static final String[] IMAGE_TYPES = new String[] { ".JPG", ".PNG",
            ".GIF", ".BMP" };

    /**
     * OFFICE软件后缀
     */
    public static final String[] OFFICE_TYPES = new String[] { ".DOC", ".DOCX",
            ".XLS", ".TXT", ".RTF", ".PDF", ".PPT", "PPTX", "XLSX" };

    /**
     * 常用格式（包含图片，Office，压缩格式）
     */
    public static final String[] NORMAL_TYPES = new String[] { ".JPG", ".PNG",
            ".GIF", ".BMP", ".DOC", ".DOCX", ".XLS", ".TXT", ".RTF", ".PDF",
            ".PPT", "PPTX", "XLSX", ".ZIP", ".ZIPX", ".RAR", "7Z" };

    
    
    public static String uploadImg(MultipartFile[] source,String root){
		String url = "";
		try {			
			//通过UUID获得一个唯一的图片文件名
			for (int i = 0; i < source.length; i++) {				
				MultipartFile multipartFile = source[i];				
				String folder="/publishpic/";
				String imgUrl=FileUtil.upload(multipartFile,folder,root);
				url=url+imgUrl+",";				
			}
			if(StringUtils.isNotEmpty(url)){
				url = url.substring(0,url.length()-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}
    
    
    public static String uploadImgOnly( HttpServletRequest request,MultipartFile file){
		String url = "";
		try {			
			//通过UUID获得一个唯一的图片文件名
		  String folder="/publishpic/";
		  String root = request.getServletContext().getRealPath("/WEB-INF/publishpic/");
		  System.out.print("获取地址为:"+root);
          if(!createFolder(root)){
              return null;
          }
          CommonsMultipartFile mf = (CommonsMultipartFile) file;
          String name = mf.getOriginalFilename();
          // 取得后缀
          String postfix = null;
          postfix = name.substring(name.lastIndexOf(".")).toLowerCase();
          // 如果不是图片格式
          if (!postfix.equals(".jpg") && !postfix.equals(".gif")
                  && !postfix.equals(".png")) {
              return "2";
          }
          // 保存图片名称
          String filename = UUID.randomUUID().toString() + postfix;
          String path = root+"\\"+filename;
          File files = new File(path);
          mf.getFileItem().write(files);
          String imgUrl = (folder+"/"+filename).replace("//", "/");
		  url=url+imgUrl+",";	
		  if(StringUtils.isNotEmpty(url)){
			url = url.substring(0,url.length()-1);
		   }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}
    
    /**
     * 上传文件
     * 
     * @param request
     *            上传文件请求
     * @param path
     *            保存路径（父级文件夹可以不存在）以'/'结尾
     * @param types
     *            可上传类型
     * @param map
     *            KEY：上传文件的name,VALUE:保存文件名，不包括后缀
     * @throws IOException
     *             文件写入异常
     */
    public static void upload(HttpServletRequest request, String path,
            String[] types, Map<String, Object> map) throws IOException {

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        MultipartFile file = null;

        if (!createFolder(path)){
            return;// 创建目录失败
        }
        
        for (Iterator<String> iterator = map.keySet().iterator(); iterator
                .hasNext();) {

            String key = iterator.next();

            file = multipartRequest.getFile(key);

            int point = 0;
            String postfix = "";
            if ((point = file.getOriginalFilename().lastIndexOf(".")) != -1){
                postfix = file.getOriginalFilename().substring(point);
            }
            if (isAllowed(postfix, types)) {

                if (map.get(key) == null){
                    map.put(key, System.currentTimeMillis());
                }
                File f = new File(path + map.get(key) + postfix);

                map.put(key, f);

                file.transferTo(f);

            }

        }

    }

    /**
     * 上传单个文件
     * 
     * @param request
     *            上传文件请求
     * @param path
     *            保存路径（父级文件夹可以不存在）以'/'结尾
     * @param types
     *            可上传类型
     * @param key
     *            上传文件的name
     * @param name
     *            保存文件名，不包括后缀
     * @return 文件
     * @throws IOException
     *             文件写入异常
     */
    public static File upload(HttpServletRequest request, String path,
            String[] types, String key, String name) throws IOException {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, name);

        upload(request, path, types, map);
        return (File) map.get(key);
    }

    /**
     * 是否允许
     * @param postfix   后缀
     * @param types     类型
     * @return          boolean
     */
    private static boolean isAllowed(String postfix, String... types) {

        if (types == null || types.length == 0){
            return true;
        }
        for (String type : types) {

            if (postfix.equalsIgnoreCase(type)){
                return true;
            }
        }

        return false;

    }

    /**
     * 创建文件夹
     * @param folderPath    路径
     * @return  boolean
     */
    private static boolean createFolder(String folderPath) {
        boolean result = false;
        
        File f = new File(folderPath);
        result = f.exists();
        if (!result) {
            result = f.mkdirs();
        }
        
        return result;
    }

    /**
     * 上传文件
     * @param request   请求
     * @param fileName
     *            文件名
     * @param folder
     *            文件夹
     * @return 数据库中的路径（null 上传失败 ，1没有上传文件，2上传不是图片类型）
     * @author 
     */
    public static String upload(HttpServletRequest request, String fileName,
            String folder, String root) {
        try {
            MultipartFile file = null;
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            file = multipartRequest.getFile(fileName);
            if (file == null || file.getSize() == 0) {
                return "1";
            }
            // 取得根目录
//           String root = request.getSession().getServletContext()
//                    .getRealPath("/uploadFile/" + folder)
//                    + "/";
            String dir = root+folder;	//图片存放目录
            if(!createFolder(dir)){
                return null;
            }
            
            CommonsMultipartFile mf = (CommonsMultipartFile) file;
            String name = mf.getOriginalFilename();
            // 取得后缀
            String postfix = null;
            postfix = name.substring(name.lastIndexOf(".")).toLowerCase();
            // 如果不是图片格式
            if (!postfix.equals(".jpg") && !postfix.equals(".gif")
                    && !postfix.equals(".png")) {
                return "2";
            }
            // 保存图片名称
            String filename = UUID.randomUUID().toString() + postfix;
            String path = dir+"\\"+filename;
            File files = new File(path);
            mf.getFileItem().write(files);
            String imgUrl = (folder+"/"+filename).replace("//", "/");
            return imgUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 上传文件
     * @param request   请求
     * @param fileName
     *            文件名
     * @param folder
     *            文件夹
     * @return 数据库中的路径（null 上传失败 ，1没有上传文件，2上传不是图片类型）
     * @author 
     */
    public static String upload(MultipartFile file,
            String folder, String root) {
        try {
            if (file == null || file.getSize() == 0) {
                return "1";
            }
            // 取得根目录
//           String root = request.getSession().getServletContext()
//                    .getRealPath("/uploadFile/" + folder)
//                    + "/";
            String dir = root+folder;	//图片存放目录
            if(!createFolder(dir)){
                return null;
            }
            
            CommonsMultipartFile mf = (CommonsMultipartFile) file;
            String name = mf.getOriginalFilename();
            // 取得后缀
            String postfix = null;
            postfix = name.substring(name.lastIndexOf(".")).toLowerCase();            
            // 如果不是图片格式
           /* if (!postfix.equals(".jpg") && !postfix.equals(".gif")
                    && !postfix.equals(".png")) {
                return "2";
            }*/
            // 保存图片名称
            String filename = UUID.randomUUID().toString() + postfix;
            String path = dir+"\\"+filename;
            File files = new File(path);
            mf.getFileItem().write(files);
            String imgUrl = (folder+"/"+filename).replace("//", "/");
           // String imgUrl = "/"+(folder+"/"+filename).replace("//", "/");
            return imgUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 上传文件（返回全路径）
     * @param request   请求
     * @param fileName
     *            文件名
     * @param folder
     *            文件夹
     * @return 数据库中的路径（null 上传失败 ，1没有上传文件，2上传不是图片类型）
     * @author 
     */
    public static String upload2(HttpServletRequest request, String fileName,
            String folder, String root) {
        try {
            MultipartFile file = null;
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            file = multipartRequest.getFile(fileName);
            if (file == null || file.getSize() == 0) {
                return "1";
            }
            // 取得根目录
//           String root = request.getSession().getServletContext()
//                    .getRealPath("/uploadFile/" + folder)
//                    + "/";
            String dir = root+folder;	//图片存放目录
            if(!createFolder(dir)){
                return null;
            }
            
            CommonsMultipartFile mf = (CommonsMultipartFile) file;
            String name = mf.getOriginalFilename();
            // 取得后缀
            String postfix = null;
            postfix = name.substring(name.lastIndexOf(".")).toLowerCase();
            // 如果不是图片格式
            if (!postfix.equals(".jpg") && !postfix.equals(".gif")
                    && !postfix.equals(".png")) {
                return "2";
            }
            // 保存图片名称
            String filename = new SimpleDateFormat("yyyyMMddHHmmss")
                    .format(new Date()) + postfix;
            String path = dir+"\\"+filename;
            File files = new File(path);
            mf.getFileItem().write(files);
            String imgUrlAll = request.getScheme()
                    + "://"
                    + request.getServerName()
                    + ":"
                    + request.getServerPort()+"/"
                    + (folder+"/"+filename)
                            .replace("//", "/");
            return imgUrlAll;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除文件
     * 
     * @param filename
     *            文件名称
     * @param folder
     *            文件夹
     * @param request   请求
     * @return true 删除成功 false删除失败
     * @author 
     */
    public static boolean deleteFile(String filename, String folder,
            HttpServletRequest request) {
        try {
            String root = "E:\\upload/" + folder + "/";
            String name = filename.substring(filename.lastIndexOf("/"),
                    filename.length());
            File file = new File(root + name);
            if (file.isFile() && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 下载文件
     * 
     * @param filePath
     *            文件绝对路径
     * @param fileName
     *            下载文件名称
     * @param response  响应
     * @throws IOException  异常
     */
    public static void downFile(String filePath, String fileName,
            HttpServletResponse response) throws IOException {

        OutputStream os = null;
        InputStream in = null;
        try {
            os = response.getOutputStream();
            response.reset();
            response.setHeader("Content-Disposition", "attachment; filename="
                    + new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));
            response.setContentType("application/octet-stream; charset=UTF-8");

            in = new FileInputStream(filePath);
            byte[] b = new byte[in.available()];

            in.read(b);

            os.write(b);
            os.flush();

        } finally {
            if (os != null)
                os.close();
            if (in != null)
                in.close();
        }

    }
    
    public static String uploadByFile(MultipartFile file, String fileName,
            String folder, String root) {
        try {
            if (file == null || file.getSize() == 0) {
                return "1";
            }
            // 取得根目录
//           String root = request.getSession().getServletContext()
//                    .getRealPath("/uploadFile/" + folder)
//                    + "/";
            String dir = root+folder;	//图片存放目录
            if(!createFolder(dir)){
                return null;
            }
            
            CommonsMultipartFile mf = (CommonsMultipartFile) file;
            String name = mf.getOriginalFilename();
            // 取得后缀
            String postfix = null;
            postfix = name.substring(name.lastIndexOf(".")).toLowerCase();
            // 如果不是图片格式
            if (!postfix.equals(".jpg") && !postfix.equals(".gif")
                    && !postfix.equals(".png")) {
                return "2";
            }
            // 保存图片名称
            String filename = UUID.randomUUID().toString() + postfix;
            String path = dir+"\\"+filename;
            File files = new File(path);
            mf.getFileItem().write(files);
            String imgUrl = "/"+(folder+"/"+filename).replace("//", "/");
            return imgUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 上传头像
     * @param request   请求
     * @param fileName
     *            文件名
     * @param folder
     *            文件夹
     * @return 数据库中的路径（null 上传失败 ，1没有上传文件，2上传不是图片类型）
     * @author 
     */
    /*public static String uploads(HttpServletRequest request, String fileName,
            String folder, String abc) {
        try {
            MultipartFile file = null;
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            file = multipartRequest.getFile(fileName);
            if (file == null || file.getSize() == 0) {
                return null;
            }
            // 取得根目录
            String root = request.getSession().getServletContext()
                    .getRealPath("/upload/" + folder)
                    + "/";
            if(!createFolder(root)){
                return null;
            }
            CommonsMultipartFile mf = (CommonsMultipartFile) file;
            String filename = null;
            filename = root + abc;
            File files = new File(filename);
            mf.getFileItem().write(files);
            String imgurl = request.getScheme()
                    + "://"
                    + request.getServerName()
                    + ":"
                    + request.getServerPort()
                    + (request.getContextPath() + "/upload/" + folder + "/" + abc)
                            .replace("//", "/");
            return imgurl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/
    
}
