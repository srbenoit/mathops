package dev.mathops.core;

/**
 * MIME types the servlet service can serve from the public file directory.
 */
public enum EMimeType {

    /** CSS stylesheet. */
    TEXTCSS("css", "text/css"),

    /** CSV file. */
    TEXTCSV("csv", "text/csv"),

    /** HTML page. */
    TEXTHTM("htm", "text/html"),

    /** HTML page. */
    TEXTHTML("html", "text/html"),

    /** Plain text. */
    TEXTPLAIN("txt", "text/plain"),

    /** Plain text log file. */
    TEXTLOG("log", "text/plain"),

    /** VTT closed-caption track. */
    TEXTVTT("vtt", "text/vtt"),

    /** XML file. */
    TEXTXML("xml", "text/xml"),

    //

    /** GIF image. */
    IMAGEGIF("gif", "image/gif"),

    /** JPEG image. */
    IMAGEJPG("jpg", "image/jpeg"),

    /** JPEG image. */
    IMAGEJPEG("jpeg", "image/jpeg"),

    /** PNG Image. */
    IMAGEPNG("png", "image/png"),

    /** GIF image. */
    IMAGESVG("svg", "image/svg+xml"),

    /** Icon. */
    IMAGEICO("ico", "image/x-icon"),

    /** Image wildcard. */
    IMAGEWILDCARD("*", "image/*"),

    //

    /** AC3 audio. */
    AUDIOAC3("ac3", "audio/ac3"),

    /** MP4 audio. */
    AUDIOMP4("m4a", "audio/mp4"),

    /** MPEG audio. */
    AUDIOMP3("mp3", "audio/mpeg"),

    /** MPEG audio. */
    AUDIOM3A("m3a", "audio/mpeg"),

    /** OGG audio. */
    AUDIOOGG("ogg", "audio/ogg"),

    /** OGG audio. */
    AUDIOOGA("oga", "audio/ogg"),

    /** AAC audio. */
    AUDIOAAC("aac", "audio/aac"),

    /** Audio wildcard. */
    AUDIOWILDCARD("*", "audio/*"),

    //

    /** H263 video. */
    VIDEOH263("h263", "video/h263"),

    /** H264 video. */
    VIDEOH264("h264", "video/h264"),

    /** MPEG 4 video. */
    VIDEOMP4("mp4", "video/mp4"),

    /** MPEG video. */
    VIDEOMPEG("mpeg", "video/mpeg"),

    /** MPEG video. */
    VIDEOMPG("mpg", "video/mpeg"),

    /** Ogg video. */
    VIDEOOGV("ogv", "video/ogg"),

    /** QuickTime video. */
    VIDEOQUICKTIME("mov", "video/quicktime"),

    /** WebM video. */
    VIDEOWEBM("webm", "video/webm"),

    /** WMV video. */
    VIDEOWMV("wmv", "video/x-ms-wmv"),

    /** Video wildcard. */
    VIDEOWILDCARD("*", "video/*"),

    //

    /** OTF font. */
    FONTOTF("otf", "font/otf"),

    /** TTF font. */
    FONTTTF("ttf", "font/ttf"),

    /** WOFF font. */
    FONTWOFF("woff", "font/woff"),

    /** WOFF2 font. */
    FONTWOFF2("woff2", "font/woff2"),

    //

    /** Model (X3D+XML). */
    MODELX3DXML("x3d", "model/x3d+xml"),

    //

    /** JavaScript script. */
    MULTIPLARTFORMDATA(null, "multipart/form-data"),

    /** JavaScript script. */
    MULTIPLARTMIXED(null, "multipart/mixed"),

    //

    /** Word document. */
    APPDOC("doc", "application/msword"),

    /** Word document. */
    APPDOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

    /** Excel spreadsheet. */
    APPXSL("xls", "application/vnd.ms-excel"),

    /** Excel spreadsheet. */
    APPXSLX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

    /** PowerPoint. */
    APPPPT("ppt", "application/vnd.ms-powerpoint"),

    /** PowerPoint. */
    APPPPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),

    /** PDF document. */
    APPPDF("pdf", "application/pdf"),

    /** XML document. */
    APPXML("xml", "application/xml"),

    /** JavaScript script. */
    APPJS("js", "application/javascript"),

    /** GZIP archive. */
    APPGZIP("gz", "application/gzip"),

    /** Jar archive. */
    APPJAR("jar", "application/java-archive");

    /** The file extension. */
    private final String ext;

    /** The MIME type. */
    public final String mime;

    /**
     * Constructs a new {@code EMimeType}.
     *
     * @param theExt  the extension (could be {@code null})
     * @param theMime the MIME type
     */
    EMimeType(final String theExt, final String theMime) {

        this.ext = theExt;
        this.mime = theMime;
    }

    /**
     * Looks up a MIME type from a file extension.
     *
     * @param ext the extension
     * @return the MIME type
     * @throws IllegalArgumentException if no type corresponds to the string
     */
    public static EMimeType forExtension(final String ext) throws IllegalArgumentException {

        if (ext == null) {
            throw new IllegalArgumentException("Extension may not be null");
        }

        EMimeType result = null;
        for (final EMimeType value : values()) {
            if (ext.equals(value.ext)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
