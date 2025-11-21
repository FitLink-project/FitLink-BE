package com.fitlink.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FitnessVideoResponseDTO {
    private Response response;

    @Getter
    @Setter
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @Setter
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @Setter
    public static class Body {
        private int pageNo;
        private int totalCount;
        private Items items;
        private int numOfRows;
    }

    @Getter
    @Setter
    public static class Items {
        private List<Item> item;
    }

    @Getter
    @Setter
    public static class Item {
        private String rptt_tcnt_nm;
        private String file_url;
        private String vdo_desc;
        private int file_sz;
        private double fps_cnt;
        private int row_num;
        private String resolution;
        private String tool_nm;
        private String aggrp_nm;
        private int frme_no;
        private String img_file_nm;
        private String fbctn_yr;
        private String vdo_len;
        private String lang;
        private String trng_nm;
        private String job_ymd;
        private String ftns_fctr_nm;
        private String vdo_ttl_nm;
        private double snap_tm;
        private String file_type_nm;
        private String file_nm;
        private String img_file_url;
        private int img_file_sn;
        private String data_type;
        private String chck_se_nm;
        private String msrmt_part_nm;
    }
}
