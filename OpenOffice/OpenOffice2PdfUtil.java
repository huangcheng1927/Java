
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * author: hc
 * description: OpenOffice2PdfUtil
 * date: 2017/9/22
 **/
public class OpenOffice2PdfUtil {
    /**
     * 支持转换的格式
     */
    private static final String[] OFFICE_SUFFIXS = {"doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "png", "jpeg", "bmp", "gif"};
    private static ArrayList<String> OFFICE_FORMATS = new ArrayList<String>();
    public static final String FILE_SUFFIXS = "pdf";
    public static String PDFDIR = "/pdfjs/temp/";

    // openOffice 安装路径
    private static String OFFICEHOME_WINDOWS;
    private static String OFFICEHOME_LINUX;

    // 初始化 获取配置文件
    static {
        OFFICEHOME_WINDOWS = PropertiesManager.getSysConfig("office_windows_home");
        OFFICEHOME_LINUX = PropertiesManager.getSysConfig("office_linux_home");
    }

    /**
     * 获取操作系统的名称
     */
    public static String getOpenOfficeHome() {
        String osName = System.getProperty("os.name");
        if (Pattern.matches("Linux.*", osName)) {
            return OFFICEHOME_LINUX;
        } else if (Pattern.matches("Windows.*", osName)) {
            return OFFICEHOME_WINDOWS;
        }
        return null;
    }


    /**
     * 输入流转换成文件
     *
     * @param in
     * @param realFilePath
     */
    public static void inputStream2File(InputStream in, Blob bin, String realFilePath) {
        FileOutputStream out = null;
        File file = null;
        try {
            file = new File(realFilePath);
            out = new FileOutputStream(file);
            int size = SimpleTypeConvert.convert2Integer(bin.length());
            byte[] buf = new byte[size];
            int len = 0;
            while ((len = in.read(buf)) >= 0) {
                out.write(buf);
            }
        } catch (Exception e) {
            throw new AppException("输入流转换成文件出错！");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建目标文件路径
     *
     * @param fileName 文件名称
     * @param type     类型 1=源文件路径 2=源文件所在目录
     * @return
     */
    public static String createSourceFile(String fileName, int type) {
        // 获取工程根目录 兼容weblogic
        String classPath = OpenOffice2PdfUtil.class.getClassLoader().getResource("/").getPath();
        String realPath = classPath.substring(0, classPath.indexOf("WEB-INF"));
        // pdf存放路径
        String path = realPath + PDFDIR;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        if (type == 1) {
            return path + fileName;
        } else {
            return path;
        }
    }

    /**
     * 目标文件路径
     *
     * @return
     */
    public static String createTargetFile() {
        return UUID.randomUUID().toString() + ".pdf";
    }

    /**
     * 转换文件
     *
     * @param inputFilePath
     * @param outputFilePath
     * @param converter
     */
    public static void converterFile(String inputFilePath, String outputFilePath, OfficeDocumentConverter converter) {
        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputFilePath);
        // 假如目标路径不存在,则新建该路径
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        converter.convert(inputFile, outputFile);
    }


    /**
     * 使Office2003-2007全部格式的文档(.doc|.docx|.xls|.xlsx|.ppt|.pptx) 包括图片等转化为pdf文件
     *
     * @param inputFilePath
     * @param outputFilePath
     * @return
     */
    public static boolean openOffice2Pdf(String inputFilePath, String outputFilePath) {
        // 连接OpenOffice并且启动OpenOffice
        DefaultOfficeManagerConfiguration config = new DefaultOfficeManagerConfiguration();
        // 获取OpenOffice的安装目录
        String officeHome = getOpenOfficeHome();
        config.setOfficeHome(officeHome);
        // 启动OpenOffice的服务
        OfficeManager officeManager = config.buildOfficeManager();
        officeManager.start();
        // 连接OpenOffice
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        File inputFile = new File(inputFilePath);
        boolean flag = false;
        if (inputFilePath != null && inputFile.exists()) {
            Collections.addAll(OFFICE_FORMATS, OFFICE_SUFFIXS);
            // 判断目标文件路径是否为空
            if (OFFICE_FORMATS.contains(getSuffix(inputFilePath))) {
                if (outputFilePath == null) {
                    // 转换后的文件路径
                    String outputFilePath_new = generateDefaultOutputFilePath(inputFilePath);
                    converterFile(inputFilePath, outputFilePath_new, converter);
                    flag = true;
                } else {
                    converterFile(inputFilePath, outputFilePath, converter);
                    flag = true;
                }
            }
        } else {
            throw new AppException("源文件不存在！");
        }
        officeManager.stop();
        return flag;
    }

    /**
     * 如果未设置输出文件路径则按照源文件路径和文件名生成输出文件地址
     */
    public static String generateDefaultOutputFilePath(String inputFilePath) {
        String outputFilePath = inputFilePath.replaceAll("." + getSuffix(inputFilePath), "_" + getSuffix(inputFilePath) + ".pdf");
        return outputFilePath;
    }

    /**
     * 获取文件的后缀名
     */
    public static String getSuffix(String inputFilePath) {
        String[] p = inputFilePath.split("\\.");
        String ext = null;
        if (p.length > 0) {
            ext = p[p.length - 1];
        }
        return ext;
    }

    public static void main(String[] args) {
        String filePath = "C:\\Users\\hcpc\\Desktop\\test\\";
        // OpenOffice2PdfUtil.openOffice2Pdf(filePath + "github.jpg", filePath + "github_" + new Date().getTime() + ".pdf");
        // OpenOffice2PdfUtil.openOffice2Pdf(filePath + "hk.png", filePath + "hk_" + new Date().getTime() + ".pdf");
        // OpenOffice2PdfUtil.openOffice2Pdf(filePath + "BaseController.odt", filePath + "BaseController_" + new Date().getTime() + ".pdf");
        // OpenOffice2PdfUtil.openOffice2Pdf(filePath + "backup.odt", filePath + "backup_" + new Date().getTime() + ".pdf");
        // OpenOffice2PdfUtil.openOffice2Pdf(filePath + "工作流.xlsx", filePath + "工作流_" + new Date().getTime() + ".pdf");
    }

}
