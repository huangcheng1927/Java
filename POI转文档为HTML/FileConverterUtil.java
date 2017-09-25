
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.converter.core.BasicURIResolver;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;


/**
 * Created by hcpc on 2017/8/28.
 */
public class FileConverterUtil {

    /**
     * 枚举文件扩展名
     */
    private enum EXT {
        ext_doc, ext_docx, ext_xls, ext_xlsx,
        ext_png, ext_jpg, ext_jpeg, ext_gif, ext_bmp,ext_pdf
    }

    /**
     * 文件扩展名静态变量
     */
    public final static String EXT_DOC = "doc";
    public final static String EXT_DOCX = "docx";
    public final static String EXT_XLS = "xls";
    public final static String EXT_XLSX = "xlsx";
    public final static String EXT_PNG = "png";
    public final static String EXT_JPG = "jpg";
    public final static String EXT_JPEG = "jpeg";
    public final static String EXT_GIF = "gif";
    public final static String EXT_BMP = "bmp";
    public final static String EXT_PDF = "pdf";

    /**
     * 扩展名map
     *
     * @param type 0=全部类型 1=文档类型  2=图片类型  3=pdf类型
     * @return
     */
    public final static Map getExtMap(int type) {
        Map<EXT, String> map = null;
        switch (type) {
            case 0:
                map = new EnumMap<EXT, String>(EXT.class);
                map.put(EXT.ext_doc, EXT_DOC);
                map.put(EXT.ext_docx, EXT_DOCX);
                map.put(EXT.ext_xls, EXT_XLS);
                map.put(EXT.ext_xlsx, EXT_XLSX);
                map.put(EXT.ext_png, EXT_PNG);
                map.put(EXT.ext_jpg, EXT_JPG);
                map.put(EXT.ext_jpeg, EXT_JPEG);
                map.put(EXT.ext_gif, EXT_GIF);
                map.put(EXT.ext_bmp, EXT_BMP);
                map.put(EXT.ext_pdf, EXT_PDF);
                break;
            case 1:
                map = new EnumMap<EXT, String>(EXT.class);
                map.put(EXT.ext_doc, EXT_DOC);
                map.put(EXT.ext_docx, EXT_DOCX);
                map.put(EXT.ext_xls, EXT_XLS);
                map.put(EXT.ext_xlsx, EXT_XLSX);
                break;
            case 2:
                map = new EnumMap<EXT, String>(EXT.class);
                map.put(EXT.ext_png, EXT_PNG);
                map.put(EXT.ext_jpg, EXT_JPG);
                map.put(EXT.ext_jpeg, EXT_JPEG);
                map.put(EXT.ext_gif, EXT_GIF);
                map.put(EXT.ext_bmp, EXT_BMP);
                break;
            case 3:
                map = new EnumMap<EXT, String>(EXT.class);
                map.put(EXT.ext_pdf, EXT_PDF);
                break;
            default:
                break;
        }
        return map;
    }



    /**
     * word转换html文件返回字节输出流
     *
     * @param realPath     物理路径
     * @param urlPath      访问路径
     * @param binFile      二进制的文件
     * @param fileName     文件名称
     * @param saveFileName 保存的文件名称(html及其附件)
     * @return
     */
    public static ByteArrayOutputStream wordToHtml(String realPath, String urlPath, Blob binFile, String fileName, String saveFileName) {
        // 获取文件后缀名称
        String ext = getFileExt(fileName);
        // html及其它文件保存的相对目录
        String relativeDir = "doctohtml" + File.separator + saveFileName;
        // 图片保存的相对目录
        String relativePicturesDir = relativeDir + ".files";
        // html及其它文件保存的物理路径
        String saveFileRealPath = realPath + File.separator + relativeDir;
        // 图片的物理目录
        final String picturesRealPath = saveFileRealPath + ".files";
        // 图片访问路径
        final String picturesUrlPath = urlPath + File.separator + relativePicturesDir;
        // 存放图片的目录
        File picturesDir = new File(picturesRealPath);
        // 目录不存在则创建目录
        if (!picturesDir.isDirectory()) {
            picturesDir.mkdirs();
        }
        ByteArrayOutputStream baos = null;
        InputStream in = null;
        try {
            in = binFile.getBinaryStream();
            if (FileConverterUtil.EXT_DOC.equals(ext)) {
                // 专门处理doc
                HWPFDocument wordDocument = new HWPFDocument(in);
                // document对象
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(document);
                // 保存文档中的图片
                wordToHtmlConverter.setPicturesManager(new PicturesManager() {
                    public String savePicture(byte[] content, PictureType pictureType, String suggestedName, float widthInches, float heightInches) {
                        File picFile = new File(picturesRealPath + File.separator + suggestedName);
                        if (!picFile.exists()) {
                            try {
                                FileOutputStream fos = new FileOutputStream(picFile);
                                fos.write(content);
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //return picturesPath + File.separator + suggestedName;
                        // 返回浏览器访问图片资源地址
                        return picturesUrlPath + File.separator + suggestedName;
                    }
                });
                // 转换
                wordToHtmlConverter.processDocument(wordDocument);
                Document htmlDocument = wordToHtmlConverter.getDocument();
                DOMSource domSource = new DOMSource(htmlDocument);
                baos = new ByteArrayOutputStream();
                StreamResult streamResult = new StreamResult(baos);
                // 得到转换工厂
                TransformerFactory tf = TransformerFactory.newInstance();
                // 转换器
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "YES");
                transformer.setOutputProperty(OutputKeys.METHOD, "HTML");
                transformer.transform(domSource, streamResult);
            } else if (FileConverterUtil.EXT_DOCX.equals(ext)) {
                // 加载word文档生成 XWPFDocument对象
                XWPFDocument document = new XWPFDocument(in);
                // 解析XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
                XHTMLOptions options = XHTMLOptions.create();
                options.setExtractor(new FileImageExtractor(picturesDir));
                // 文档中图片访问路径
                options.URIResolver(new BasicURIResolver(picturesUrlPath));
                options.setIgnoreStylesIfUnused(false);
                options.setFragment(true);
                // 将XWPFDocument转换成XHTML
                baos = new ByteArrayOutputStream();
                XHTMLConverter.getInstance().convert(document, baos, options);
            } else {
                baos = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return baos;
    }

    /**
     * word 转换为 html,兼容doc和docx
     *
     * @param realPath
     * @param sourceFileName
     * @param saveFileName
     * @return
     * @throws Exception
     */
    public static String wordToHtml(String realPath, String sourceFileName, String saveFileName) {
        // 获取文件后缀名称
        String ext = getFileExt(sourceFileName);
        // 源文件路径
        String sourceFilePath = realPath + File.separator + sourceFileName;
        // 保存路径
        String saveFilePath = realPath + File.separator + saveFileName;
        // 图片路径
        final String picturesPath = saveFilePath + ".files";
        // 存放图片的目录
        File picturesDir = new File(picturesPath);
        if (!picturesDir.isDirectory()) {
            picturesDir.mkdirs();
        }
        String content = null;
        try {
            if (FileConverterUtil.EXT_DOC.equals(ext)) {
                // 专门处理doc
                HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(sourceFilePath));
                // document对象
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(document);
                // 保存文档中的图片到picturesDir目录
                wordToHtmlConverter.setPicturesManager(new PicturesManager() {
                    public String savePicture(byte[] content, PictureType pictureType, String suggestedName, float widthInches, float heightInches) {
                        File picFile = new File(picturesPath + File.separator + suggestedName);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(picFile);
                            fos.write(content);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return picturesPath + File.separator + suggestedName;
                    }
                });
                // 转换
                wordToHtmlConverter.processDocument(wordDocument);
                Document htmlDocument = wordToHtmlConverter.getDocument();
                DOMSource domSource = new DOMSource(htmlDocument);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                StreamResult streamResult = new StreamResult(out);
                // 得到转换工厂
                TransformerFactory tf = TransformerFactory.newInstance();
                // 转换器
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "YES");
                transformer.setOutputProperty(OutputKeys.METHOD, "HTML");
                transformer.transform(domSource, streamResult);
                content = new String(out.toByteArray(), "UTF-8");
                // 写入
                writeFile(content, saveFilePath + ".html");
                //FileUtils.writeStringToFile(new File(realPath, "exportExcel.html"), content, "utf-8");
                //content = out.toString();
            } else if (FileConverterUtil.EXT_DOCX.equals(ext)) {
                // 1 加载word文档生成 XWPFDocument对象
                XWPFDocument document = new XWPFDocument(new FileInputStream(new File(sourceFilePath)));
                // 2 解析 XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
                XHTMLOptions options = XHTMLOptions.create();//.indent(4);
                options.setExtractor(new FileImageExtractor(picturesDir));
                options.URIResolver(new BasicURIResolver(picturesPath));
                options.URIResolver(new BasicURIResolver(saveFilePath + ".files"));
                options.setIgnoreStylesIfUnused(false);
                options.setFragment(true);
                // 3 将 XWPFDocument转换成XHTML
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XHTMLConverter.getInstance().convert(document, baos, options);
                baos.close();
                content = baos.toString("UTF-8");
                System.out.println(content);
                writeFile(content, saveFilePath + ".html");
            } else {
                content = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * excel转换成html并返回字符串
     *
     * @param binFile
     * @param isWithStyle
     * @return
     */
    public static String excelToHtml(Blob binFile, boolean isWithStyle) {
        String htmlExcel = null;
        try {
            Workbook work = WorkbookFactory.create(binFile.getBinaryStream());
            if (work instanceof HSSFWorkbook) {
                HSSFWorkbook hWb = (HSSFWorkbook) work;
                htmlExcel = FileConverterUtil.getExcelInfo(hWb, isWithStyle);
            } else if (work instanceof XSSFWorkbook) {
                XSSFWorkbook xWb = (XSSFWorkbook) work;
                htmlExcel = FileConverterUtil.getExcelInfo(xWb, isWithStyle);
            }
            //FileUtils.writeStringToFile(new File(filePath + saveFileName), htmlExcel, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlExcel;
    }


    /**
     * Excel转换为html,兼容xls和xlsx,样式兼容有差异
     *
     * @param filePath
     * @param sourceFileName
     * @param saveFileName
     * @param isWithStyle
     * @return
     */
    public static String excelToHtml(String filePath, String sourceFileName, String saveFileName, boolean isWithStyle) {
        InputStream fis = null;
        String htmlExcel = null;
        try {
            String filePathName = filePath + sourceFileName;
            fis = new FileInputStream(new File(filePathName));
            Workbook wb = WorkbookFactory.create(fis);
            if (wb instanceof XSSFWorkbook) {  // xlxs
                XSSFWorkbook xWb = (XSSFWorkbook) wb;
                htmlExcel = FileConverterUtil.getExcelInfo(xWb, isWithStyle);
            } else if (wb instanceof HSSFWorkbook) { // xls
                HSSFWorkbook hWb = (HSSFWorkbook) wb;
                htmlExcel = FileConverterUtil.getExcelInfo(hWb, isWithStyle);
            }
            //  writeFile(htmlExcel, filePath + saveFileName);
            FileUtils.writeStringToFile(new File(filePath + saveFileName), htmlExcel, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlExcel;
    }

    /**
     * 取得Excel内容
     *
     * @param wb
     * @param isWithStyle
     * @return
     */
    public static String getExcelInfo(Workbook wb, boolean isWithStyle) {
        StringBuffer sb = new StringBuffer();
        // 获取第一个Sheet的内容
        Sheet sheet = wb.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        Map<String, String> map[] = getRowSpanAndColSpanMap(sheet);
        sb.append("<table style='border-collapse:collapse;' width='100%'>");
        Row row = null; // 兼容
        Cell cell = null; // 兼容
        for (int rowNum = sheet.getFirstRowNum(); rowNum <= lastRowNum; rowNum++) {
            row = sheet.getRow(rowNum);
            if (row == null) {
                sb.append("<tr><td > &nbsp;</td></tr>");
                continue;
            }
            sb.append("<tr>");
            int lastColNum = row.getLastCellNum();
            for (int colNum = 0; colNum < lastColNum; colNum++) {
                cell = row.getCell(colNum);
                if (cell == null) { // 特殊情况 空白的单元格会返回null
                    sb.append("<td>&nbsp;</td>");
                    continue;
                }
                String stringValue = getCellValue(cell);
                if (map[0].containsKey(rowNum + "," + colNum)) {
                    String pointString = map[0].get(rowNum + "," + colNum);
                    map[0].remove(rowNum + "," + colNum);
                    int bottomeRow = Integer.valueOf(pointString.split(",")[0]);
                    int bottomeCol = Integer.valueOf(pointString.split(",")[1]);
                    int rowSpan = bottomeRow - rowNum + 1;
                    int colSpan = bottomeCol - colNum + 1;
                    sb.append("<td rowspan= '" + rowSpan + "' colspan= '" + colSpan + "' ");
                } else if (map[1].containsKey(rowNum + "," + colNum)) {
                    map[1].remove(rowNum + "," + colNum);
                    continue;
                } else {
                    sb.append("<td ");
                }
                // 判断是否需要样式
                if (isWithStyle) {
                    // 处理单元格样式
                    dealExcelStyle(wb, sheet, cell, sb);
                }
                sb.append(">");
                if (stringValue == null || "".equals(stringValue.trim())) {
                    sb.append(" &nbsp; ");
                } else {
                    // 将ascii码为160的空格转换为html下的空格（&nbsp;）
                    sb.append(stringValue.replace(String.valueOf((char) 160), "&nbsp;"));
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    /**
     * 获取行合列
     *
     * @param sheet
     * @return
     */
    private static Map<String, String>[] getRowSpanAndColSpanMap(Sheet sheet) {
        Map<String, String> mapRow = new HashMap<String, String>();
        Map<String, String> mapCol = new HashMap<String, String>();
        int mergedNum = sheet.getNumMergedRegions();
        CellRangeAddress range = null;
        for (int i = 0; i < mergedNum; i++) {
            range = sheet.getMergedRegion(i);
            int topRow = range.getFirstRow();
            int topCol = range.getFirstColumn();
            int bottomRow = range.getLastRow();
            int bottomCol = range.getLastColumn();
            mapRow.put(topRow + "," + topCol, bottomRow + "," + bottomCol);
            // System.out.println(topRow + "," + topCol + "," + bottomRow + ","
            // + bottomCol);
            int tempRow = topRow;
            while (tempRow <= bottomRow) {
                int tempCol = topCol;
                while (tempCol <= bottomCol) {
                    mapCol.put(tempRow + "," + tempCol, "");
                    tempCol++;
                }
                tempRow++;
            }
            mapCol.remove(topRow + "," + topCol);
        }
        Map[] map = {mapRow, mapCol};
        return map;
    }

    /**
     * 获取表格单元格内容
     *
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell) {
        String result = new String();
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:// 数字类型
                if (HSSFDateUtil.isCellDateFormatted(cell)) {// 处理日期格式、时间格式
                    SimpleDateFormat sdf = null;
                    if (cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("h:mm")) {
                        sdf = new SimpleDateFormat("HH:mm");
                    } else {// 日期
                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                    }
                    Date date = cell.getDateCellValue();
                    result = sdf.format(date);
                } else if (cell.getCellStyle().getDataFormat() == 58) {
                    // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    double value = cell.getNumericCellValue();
                    Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
                    result = sdf.format(date);
                } else {
                    double value = cell.getNumericCellValue();
                    CellStyle style = cell.getCellStyle();
                    DecimalFormat format = new DecimalFormat();
                    String temp = style.getDataFormatString();
                    // 单元格设置成常规
                    if (temp.equals("General")) {
                        format.applyPattern("#");
                    }
                    result = format.format(value);
                }
                break;
            case Cell.CELL_TYPE_STRING:// String类型
                result = cell.getRichStringCellValue().toString();
                break;
            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

    /**
     * 处理表格样式
     *
     * @param wb
     * @param sheet
     * @param cell
     * @param sb
     */
    private static void dealExcelStyle(Workbook wb, Sheet sheet, Cell cell, StringBuffer sb) {
        CellStyle cellStyle = cell.getCellStyle();
        if (cellStyle != null) {
            short alignment = cellStyle.getAlignment();
            // 单元格内容的水平对齐方式
            sb.append("align='" + convertAlignToHtml(alignment) + "' ");
            short verticalAlignment = cellStyle.getVerticalAlignment();
            // 单元格中内容的垂直排列方式
            sb.append("valign='" + convertVerticalAlignToHtml(verticalAlignment) + "' ");
            if (wb instanceof XSSFWorkbook) {
                XSSFFont xf = ((XSSFCellStyle) cellStyle).getFont();
                short boldWeight = xf.getBoldweight();
                sb.append("style='");
                // 字体加粗
                sb.append("font-weight:" + boldWeight + ";");
                // 字体大小
                sb.append("font-size: " + xf.getFontHeight() / 2 + "%;");
                int columnWidth = sheet.getColumnWidth(cell.getColumnIndex());
                sb.append("width:" + columnWidth + "px;");
                XSSFColor xc = xf.getXSSFColor();
                // 字体颜色
                if (xc != null && !"".equals(xc)) {
                    sb.append("color:#" + xc.getARGBHex().substring(2) + ";");
                }

                XSSFColor bgColor = (XSSFColor) cellStyle.getFillForegroundColorColor();
                // System.out.println("************************************");
                // System.out.println("BackgroundColorColor:
                // "+cellStyle.getFillBackgroundColorColor());
                // System.out.println("ForegroundColor:
                // "+cellStyle.getFillForegroundColor());//0
                // System.out.println("BackgroundColorColor:
                // "+cellStyle.getFillBackgroundColorColor());
                // System.out.println("ForegroundColorColor:
                // "+cellStyle.getFillForegroundColorColor());
                // String bgColorStr = bgColor.getARGBHex();
                // System.out.println("bgColorStr: "+bgColorStr);
                // 背景颜色
                if (bgColor != null && !"".equals(bgColor)) {
                    sb.append("background-color:#" + bgColor.getARGBHex().substring(2) + ";");
                }
                sb.append(getBorderStyle(0, cellStyle.getBorderTop(),
                        ((XSSFCellStyle) cellStyle).getTopBorderXSSFColor()));
                sb.append(getBorderStyle(1, cellStyle.getBorderRight(),
                        ((XSSFCellStyle) cellStyle).getRightBorderXSSFColor()));
                sb.append(getBorderStyle(2, cellStyle.getBorderBottom(),
                        ((XSSFCellStyle) cellStyle).getBottomBorderXSSFColor()));
                sb.append(getBorderStyle(3, cellStyle.getBorderLeft(),
                        ((XSSFCellStyle) cellStyle).getLeftBorderXSSFColor()));
            } else if (wb instanceof HSSFWorkbook) {
                HSSFFont hf = ((HSSFCellStyle) cellStyle).getFont(wb);
                short boldWeight = hf.getBoldweight();
                short fontColor = hf.getColor();
                sb.append("style='");
                // 类HSSFPalette用于求的颜色的国际标准形式
                HSSFPalette palette = ((HSSFWorkbook) wb).getCustomPalette();
                HSSFColor hc = palette.getColor(fontColor);
                // 字体加粗
                sb.append("font-weight:" + boldWeight + ";");
                // 字体大小
                sb.append("font-size: " + hf.getFontHeight() / 2 + "%;");
                String fontColorStr = convertToStardColor(hc);
                // 字体颜色
                if (fontColorStr != null && !"".equals(fontColorStr.trim())) {
                    sb.append("color:" + fontColorStr + ";");
                }
                int columnWidth = sheet.getColumnWidth(cell.getColumnIndex());
                sb.append("width:" + columnWidth + "px;");
                short bgColor = cellStyle.getFillForegroundColor();
                hc = palette.getColor(bgColor);
                String bgColorStr = convertToStardColor(hc);
                // 背景颜色
                if (bgColorStr != null && !"".equals(bgColorStr.trim())) {
                    sb.append("background-color:" + bgColorStr + ";");
                }
                sb.append(getBorderStyle(palette, 0, cellStyle.getBorderTop(), cellStyle.getTopBorderColor()));
                sb.append(getBorderStyle(palette, 1, cellStyle.getBorderRight(), cellStyle.getRightBorderColor()));
                sb.append(getBorderStyle(palette, 3, cellStyle.getBorderLeft(), cellStyle.getLeftBorderColor()));
                sb.append(getBorderStyle(palette, 2, cellStyle.getBorderBottom(), cellStyle.getBottomBorderColor()));
            }
            sb.append("' ");
        }
    }

    /**
     * 单元格内容的水平对齐方式
     *
     * @param alignment
     * @return
     */
    private static String convertAlignToHtml(short alignment) {
        String align = "left";
        switch (alignment) {
            case CellStyle.ALIGN_LEFT:
                align = "left";
                break;
            case CellStyle.ALIGN_CENTER:
                align = "center";
                break;
            case CellStyle.ALIGN_RIGHT:
                align = "right";
                break;
            default:
                break;
        }
        return align;
    }

    /**
     * 单元格中内容的垂直排列方式
     *
     * @param verticalAlignment
     * @return
     */
    private static String convertVerticalAlignToHtml(short verticalAlignment) {
        String valign = "middle";
        switch (verticalAlignment) {
            case CellStyle.VERTICAL_BOTTOM:
                valign = "bottom";
                break;
            case CellStyle.VERTICAL_CENTER:
                valign = "center";
                break;
            case CellStyle.VERTICAL_TOP:
                valign = "top";
                break;
            default:
                break;
        }
        return valign;
    }

    /**
     * 转换标准色
     *
     * @param hc
     * @return
     */
    private static String convertToStardColor(HSSFColor hc) {
        StringBuffer sb = new StringBuffer("");
        if (hc != null) {
            if (HSSFColor.AUTOMATIC.index == hc.getIndex()) {
                return null;
            }
            sb.append("#");
            for (int i = 0; i < hc.getTriplet().length; i++) {
                sb.append(fillWithZero(Integer.toHexString(hc.getTriplet()[i])));
            }
        }
        return sb.toString();
    }

    private static String fillWithZero(String str) {
        if (str != null && str.length() < 2) {
            return "0" + str;
        }
        return str;
    }

    static String[] bordesr = {"border-top:", "border-right:", "border-bottom:", "border-left:"};
    static String[] borderStyles = {"solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid ",
            "solid ", "solid", "solid", "solid", "solid", "solid"};

    /**
     * 获取边框样式
     *
     * @param palette
     * @param b
     * @param s
     * @param t
     * @return
     */
    private static String getBorderStyle(HSSFPalette palette, int b, short s, short t) {
        if (s == 0) {
            return bordesr[b] + borderStyles[s] + "#d0d7e5 1px;";
        }
        String borderColorStr = convertToStardColor(palette.getColor(t));
        borderColorStr = borderColorStr == null || borderColorStr.length() < 1 ? "#000000" : borderColorStr;
        return bordesr[b] + borderStyles[s] + borderColorStr + " 1px;";
    }

    /**
     * 边框样式
     *
     * @param b
     * @param s
     * @param xc
     * @return
     */
    private static String getBorderStyle(int b, short s, XSSFColor xc) {
        if (s == 0)
            return bordesr[b] + borderStyles[s] + "#d0d7e5 1px;";
        if (xc != null && !"".equals(xc)) {
            String borderColorStr = xc.getARGBHex();
            borderColorStr = borderColorStr == null || borderColorStr.length() < 1 ? "#000000"
                    : borderColorStr.substring(2);
            return bordesr[b] + borderStyles[s] + borderColorStr + " 1px;";
        }
        return "";
    }

    /**
     * 获取文件扩展名称
     *
     * @param name
     * @return
     */
    public static String getFileExt(String name) {
        String ext = null;
        int i = name.lastIndexOf('.');
        if (i > 0 && i < name.length() - 1) {
            ext = name.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * 输出html
     *
     * @param content
     * @param path
     */
    private static void writeFile(String content, String path) {
        OutputStream os = null;
        BufferedWriter bw = null;
        try {
            File file = new File(path);
            os = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (os != null)
                    os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * xls -> html 但是不支持 xlsx -> html
     *
     * @param realPath
     * @param sourceFileName
     * @param saveName
     * @throws Exception
     */
    public static void xlsToHtml(String realPath, String sourceFileName, String saveName) throws Exception {
        final String sourceFilePath = realPath + File.separator + sourceFileName;

        InputStream input = new FileInputStream(sourceFilePath);
        HSSFWorkbook excelBook = new HSSFWorkbook(input);
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(document);
        excelToHtmlConverter.processWorkbook(excelBook);
        List pics = excelBook.getAllPictures();
        if (pics != null) {
            for (int i = 0; i < pics.size(); i++) {
                Picture pic = (Picture) pics.get(i);
                try {
                    pic.writeImageContent(new FileOutputStream(realPath + pic.suggestFullFileName()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        Document htmlDocument = excelToHtmlConverter.getDocument();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(outStream);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "YES");
        serializer.setOutputProperty(OutputKeys.METHOD, "HTML");
        serializer.transform(domSource, streamResult);
        outStream.close();
        String content = new String(outStream.toByteArray());

        FileUtils.writeStringToFile(new File(realPath, saveName), content, "UTF-8");
    }

    /**
     *
     * @param realPath
     * @param sourceFileName
     * @param saveFileName
     * @return
     * @throws Exception
     */
    public static String docToPdf(String realPath, String sourceFileName, String saveFileName) throws Exception {
        // 1. 将源文件转换成html文件
        String content = wordToHtml(realPath, sourceFileName, saveFileName);
        System.out.println(content);
        // 文件名
        String pdfName = UUID.randomUUID().toString()+".pdf";
        // 文件路径名称
        String path = realPath + File.separator + pdfName;
        // 2. 将html文件转换成pdf文件
        if(htmlConvertPdf(content, path)){
            return path;
        } else {
            path = null;
        }
        return path;
    }


    /**
     * POI将html换成PDF
     * @param content html页面
     * @param filePath 存放生成PDF文档的路径
     */
    public static boolean htmlConvertPdf(String content, String filePath) {
        if(!ValidateUtil.isNotEmpty(content))
            return false;
        // 转换成功标识
        boolean flag = false;
        FileOutputStream fos = null;
        OutputStream out = null;
        File file = null;
        InputStream in = null;
        // itext document
        com.itextpdf.text.Document document = null;
        // jsoup 对未闭合的标签进行闭合,因为iText严格遵循XHTML规范
        org.jsoup.nodes.Document jsoupDoc = null;
        // xml语法分析器,必须遵循xml语法
        org.jsoup.nodes.Document.OutputSettings.Syntax syntax = null;
        // 写入器,其他写入器为 HtmlWriter、RtfWriter、XmlWriter
        PdfWriter writer = null;
        try {
            document = new com.itextpdf.text.Document();
            jsoupDoc = Jsoup.parse(content, "");
            // 逃逸模式
            jsoupDoc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
            //jsoupDoc.outputSettings().escapeMode(Entities.EscapeMode.base);
            jsoupDoc.outputSettings().prettyPrint(true);

            // xml语法
            syntax = org.jsoup.nodes.Document.OutputSettings.Syntax.xml;
            jsoupDoc.outputSettings().syntax(syntax);
            content = jsoupDoc.toString();
            System.out.println(content);
            file = new File(filePath);
            fos = new FileOutputStream(file);
            // 将Document实例和文件输出流PdfWriter类绑定在一起
            writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, fos);
            // 打开文档
            document.open();
            in = new ByteArrayInputStream(content.getBytes("UTF-8"));

            // 使用字体提供器，并将其设置为unicode字体样式
            MyFontsProvider fontProvider = new MyFontsProvider();
            //fontProvider.addFontSubstitute("lowagie", "garamond");
            fontProvider.setUseUnicode(true);

            // css样式解析
            CssAppliers cssAppliers = new CssAppliersImpl(fontProvider);
            HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
            htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
            XMLWorkerHelper.getInstance().getDefaultCssResolver(true);

            // 进行转换
            XMLWorkerHelper helper = XMLWorkerHelper.getInstance();
            helper.parseXHtml(writer, document, in, null, Charset.forName("UTF-8"), fontProvider);
            //关闭
            document.close();
            writer.close();
            in.close();
            flag = true;
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    public boolean fileDelete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            // 递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = fileDelete(new File(file, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return file.delete();
    }

    /**
     * 重写 getFont()字符设置方法，解决中文无法转换问题
     */
    public static class MyFontsProvider extends XMLWorkerFontProvider {
        public MyFontsProvider() {
            super(null, null);
        }
        @Override
        public Font getFont(final String fontname, String encoding, float size, final int style) {
            BaseFont baseFont = null;
            try {
                baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Font(baseFont, 12, Font.NORMAL);
        }
    }


    public static void main(String[] args) throws Exception {
        String filePath = "C:\\Users\\hcpc\\Desktop\\test\\";
        // ==============================word转html========================================
   
        String saveFileName = "root";
        //FileConverterUtil.wordToHtml(filePath, sourceFileName, saveFileName);

        // ===============================excel转HTML=======================================
       
        //String fileName = "tab.xlsx";
        //String fileName = "工作流.xlsx";
        //FileConverterUtil.excelToHtml(filePath,fileName, "index.html",true);

        //================================转PDF=========================================
        FileConverterUtil.docToPdf(filePath, sourceFileName, saveFileName);
    }
}
