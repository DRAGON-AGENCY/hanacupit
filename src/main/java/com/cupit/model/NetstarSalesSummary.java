package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_netstar_sales_summary")
public class NetstarSalesSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "netstar_summary_id")
    private int netstarSummaryId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "batch_id", nullable = false)
    private int batchId;

    @Column(name = "store_code")
    private String storeCode;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "sales_count", nullable = false)
    private int salesCount;

    @Column(name = "sales_amount", nullable = false)
    private int salesAmount;

    @Column(name = "refund_count", nullable = false)
    private int refundCount;

    @Column(name = "refund_amount", nullable = false)
    private int refundAmount;

    @Column(name = "net_amount", nullable = false)
    private int netAmount;

    @Column(name = "alipay_sales_count", nullable = false)
    private int alipaySalesCount;

    @Column(name = "alipay_sales_amount", nullable = false)
    private int alipaySalesAmount;

    @Column(name = "alipay_refund_count", nullable = false)
    private int alipayRefundCount;

    @Column(name = "alipay_refund_amount", nullable = false)
    private int alipayRefundAmount;

    @Column(name = "alipay_net_amount", nullable = false)
    private int alipayNetAmount;

    @Column(name = "dpay_sales_count", nullable = false)
    private int dpaySalesCount;

    @Column(name = "dpay_sales_amount", nullable = false)
    private int dpaySalesAmount;

    @Column(name = "dpay_refund_count", nullable = false)
    private int dpayRefundCount;

    @Column(name = "dpay_refund_amount", nullable = false)
    private int dpayRefundAmount;

    @Column(name = "dpay_net_amount", nullable = false)
    private int dpayNetAmount;

    @Column(name = "paypay_sales_count", nullable = false)
    private int paypaySalesCount;

    @Column(name = "paypay_sales_amount", nullable = false)
    private int paypaySalesAmount;

    @Column(name = "paypay_refund_count", nullable = false)
    private int paypayRefundCount;

    @Column(name = "paypay_refund_amount", nullable = false)
    private int paypayRefundAmount;

    @Column(name = "paypay_net_amount", nullable = false)
    private int paypayNetAmount;

    @Column(name = "rakuten_sales_count", nullable = false)
    private int rakutenSalesCount;

    @Column(name = "rakuten_sales_amount", nullable = false)
    private int rakutenSalesAmount;

    @Column(name = "rakuten_refund_count", nullable = false)
    private int rakutenRefundCount;

    @Column(name = "rakuten_refund_amount", nullable = false)
    private int rakutenRefundAmount;

    @Column(name = "rakuten_net_amount", nullable = false)
    private int rakutenNetAmount;

    @Column(name = "smartcode_sales_count", nullable = false)
    private int smartcodeSalesCount;

    @Column(name = "smartcode_sales_amount", nullable = false)
    private int smartcodeSalesAmount;

    @Column(name = "smartcode_refund_count", nullable = false)
    private int smartcodeRefundCount;

    @Column(name = "smartcode_refund_amount", nullable = false)
    private int smartcodeRefundAmount;

    @Column(name = "smartcode_net_amount", nullable = false)
    private int smartcodeNetAmount;

    @Column(name = "wechat_sales_count", nullable = false)
    private int wechatSalesCount;

    @Column(name = "wechat_sales_amount", nullable = false)
    private int wechatSalesAmount;

    @Column(name = "wechat_refund_count", nullable = false)
    private int wechatRefundCount;

    @Column(name = "wechat_refund_amount", nullable = false)
    private int wechatRefundAmount;

    @Column(name = "wechat_net_amount", nullable = false)
    private int wechatNetAmount;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getNetstarSummaryId() { return netstarSummaryId; }
    public void setNetstarSummaryId(int netstarSummaryId) { this.netstarSummaryId = netstarSummaryId; }
    public String getTradeCode() { return tradeCode; }
    public void setTradeCode(String tradeCode) { this.tradeCode = tradeCode; }
    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }
    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }
    public int getSalesAmount() { return salesAmount; }
    public void setSalesAmount(int salesAmount) { this.salesAmount = salesAmount; }
    public int getRefundCount() { return refundCount; }
    public void setRefundCount(int refundCount) { this.refundCount = refundCount; }
    public int getRefundAmount() { return refundAmount; }
    public void setRefundAmount(int refundAmount) { this.refundAmount = refundAmount; }
    public int getNetAmount() { return netAmount; }
    public void setNetAmount(int netAmount) { this.netAmount = netAmount; }
    public int getAlipaySalesCount() { return alipaySalesCount; }
    public void setAlipaySalesCount(int alipaySalesCount) { this.alipaySalesCount = alipaySalesCount; }
    public int getAlipaySalesAmount() { return alipaySalesAmount; }
    public void setAlipaySalesAmount(int alipaySalesAmount) { this.alipaySalesAmount = alipaySalesAmount; }
    public int getAlipayRefundCount() { return alipayRefundCount; }
    public void setAlipayRefundCount(int alipayRefundCount) { this.alipayRefundCount = alipayRefundCount; }
    public int getAlipayRefundAmount() { return alipayRefundAmount; }
    public void setAlipayRefundAmount(int alipayRefundAmount) { this.alipayRefundAmount = alipayRefundAmount; }
    public int getAlipayNetAmount() { return alipayNetAmount; }
    public void setAlipayNetAmount(int alipayNetAmount) { this.alipayNetAmount = alipayNetAmount; }
    public int getDpaySalesCount() { return dpaySalesCount; }
    public void setDpaySalesCount(int dpaySalesCount) { this.dpaySalesCount = dpaySalesCount; }
    public int getDpaySalesAmount() { return dpaySalesAmount; }
    public void setDpaySalesAmount(int dpaySalesAmount) { this.dpaySalesAmount = dpaySalesAmount; }
    public int getDpayRefundCount() { return dpayRefundCount; }
    public void setDpayRefundCount(int dpayRefundCount) { this.dpayRefundCount = dpayRefundCount; }
    public int getDpayRefundAmount() { return dpayRefundAmount; }
    public void setDpayRefundAmount(int dpayRefundAmount) { this.dpayRefundAmount = dpayRefundAmount; }
    public int getDpayNetAmount() { return dpayNetAmount; }
    public void setDpayNetAmount(int dpayNetAmount) { this.dpayNetAmount = dpayNetAmount; }
    public int getPaypaySalesCount() { return paypaySalesCount; }
    public void setPaypaySalesCount(int paypaySalesCount) { this.paypaySalesCount = paypaySalesCount; }
    public int getPaypaySalesAmount() { return paypaySalesAmount; }
    public void setPaypaySalesAmount(int paypaySalesAmount) { this.paypaySalesAmount = paypaySalesAmount; }
    public int getPaypayRefundCount() { return paypayRefundCount; }
    public void setPaypayRefundCount(int paypayRefundCount) { this.paypayRefundCount = paypayRefundCount; }
    public int getPaypayRefundAmount() { return paypayRefundAmount; }
    public void setPaypayRefundAmount(int paypayRefundAmount) { this.paypayRefundAmount = paypayRefundAmount; }
    public int getPaypayNetAmount() { return paypayNetAmount; }
    public void setPaypayNetAmount(int paypayNetAmount) { this.paypayNetAmount = paypayNetAmount; }
    public int getRakutenSalesCount() { return rakutenSalesCount; }
    public void setRakutenSalesCount(int rakutenSalesCount) { this.rakutenSalesCount = rakutenSalesCount; }
    public int getRakutenSalesAmount() { return rakutenSalesAmount; }
    public void setRakutenSalesAmount(int rakutenSalesAmount) { this.rakutenSalesAmount = rakutenSalesAmount; }
    public int getRakutenRefundCount() { return rakutenRefundCount; }
    public void setRakutenRefundCount(int rakutenRefundCount) { this.rakutenRefundCount = rakutenRefundCount; }
    public int getRakutenRefundAmount() { return rakutenRefundAmount; }
    public void setRakutenRefundAmount(int rakutenRefundAmount) { this.rakutenRefundAmount = rakutenRefundAmount; }
    public int getRakutenNetAmount() { return rakutenNetAmount; }
    public void setRakutenNetAmount(int rakutenNetAmount) { this.rakutenNetAmount = rakutenNetAmount; }
    public int getSmartcodeSalesCount() { return smartcodeSalesCount; }
    public void setSmartcodeSalesCount(int smartcodeSalesCount) { this.smartcodeSalesCount = smartcodeSalesCount; }
    public int getSmartcodeSalesAmount() { return smartcodeSalesAmount; }
    public void setSmartcodeSalesAmount(int smartcodeSalesAmount) { this.smartcodeSalesAmount = smartcodeSalesAmount; }
    public int getSmartcodeRefundCount() { return smartcodeRefundCount; }
    public void setSmartcodeRefundCount(int smartcodeRefundCount) { this.smartcodeRefundCount = smartcodeRefundCount; }
    public int getSmartcodeRefundAmount() { return smartcodeRefundAmount; }
    public void setSmartcodeRefundAmount(int smartcodeRefundAmount) { this.smartcodeRefundAmount = smartcodeRefundAmount; }
    public int getSmartcodeNetAmount() { return smartcodeNetAmount; }
    public void setSmartcodeNetAmount(int smartcodeNetAmount) { this.smartcodeNetAmount = smartcodeNetAmount; }
    public int getWechatSalesCount() { return wechatSalesCount; }
    public void setWechatSalesCount(int wechatSalesCount) { this.wechatSalesCount = wechatSalesCount; }
    public int getWechatSalesAmount() { return wechatSalesAmount; }
    public void setWechatSalesAmount(int wechatSalesAmount) { this.wechatSalesAmount = wechatSalesAmount; }
    public int getWechatRefundCount() { return wechatRefundCount; }
    public void setWechatRefundCount(int wechatRefundCount) { this.wechatRefundCount = wechatRefundCount; }
    public int getWechatRefundAmount() { return wechatRefundAmount; }
    public void setWechatRefundAmount(int wechatRefundAmount) { this.wechatRefundAmount = wechatRefundAmount; }
    public int getWechatNetAmount() { return wechatNetAmount; }
    public void setWechatNetAmount(int wechatNetAmount) { this.wechatNetAmount = wechatNetAmount; }
    public String getUpdateEmployee() { return updateEmployee; }
    public void setUpdateEmployee(String updateEmployee) { this.updateEmployee = updateEmployee; }
    public LocalDate getCreateDate() { return createDate; }
    public void setCreateDate(LocalDate createDate) { this.createDate = createDate; }
    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }
}
