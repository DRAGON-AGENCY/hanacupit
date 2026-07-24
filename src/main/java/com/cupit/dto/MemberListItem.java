package com.cupit.dto;

/**
 * 「加盟店一覧」画面の一覧行を表す DTO。
 * {@code m_member_info}は266列を持つため、一覧表示に不要な列まで含めた
 * エンティティをそのまま返さず、必要な列だけを{@code MemberInfoRepository}の
 * JPQLコンストラクタ式で射影して取得する。所在地住所は市区町村・町名・
 * 丁目番地号・建物名の4列を1つの表示用文字列に結合して保持する。
 */
public class MemberListItem {

    private final String tradeCode;
    private final String storeNameKana;
    private final String storeName;
    private final String address;
    private final String qualificationType;

    public MemberListItem(
            String tradeCode, String storeNameKana, String storeName,
            String addrCity, String addrTown, String addrBlock, String addrBuilding,
            String qualificationType) {
        this.tradeCode = tradeCode;
        this.storeNameKana = storeNameKana;
        this.storeName = storeName;
        this.address = buildAddress(addrCity, addrTown, addrBlock, addrBuilding);
        this.qualificationType = qualificationType;
    }

    private static String buildAddress(
            String addrCity, String addrTown, String addrBlock, String addrBuilding) {
        StringBuilder address = new StringBuilder();
        appendIfPresent(address, addrCity);
        appendIfPresent(address, addrTown);
        appendIfPresent(address, addrBlock);
        if (addrBuilding != null && !addrBuilding.isBlank()) {
            if (address.length() > 0) {
                address.append(' ');
            }
            address.append(addrBuilding);
        }
        return address.toString();
    }

    private static void appendIfPresent(StringBuilder address, String part) {
        if (part != null && !part.isBlank()) {
            address.append(part);
        }
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public String getStoreNameKana() {
        return storeNameKana;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getAddress() {
        return address;
    }

    public String getQualificationType() {
        return qualificationType;
    }

    @Override
    public String toString() {
        return "MemberListItem{"
                + "tradeCode='" + tradeCode + '\''
                + ", storeNameKana='" + storeNameKana + '\''
                + ", storeName='" + storeName + '\''
                + ", address='" + address + '\''
                + ", qualificationType='" + qualificationType + '\''
                + '}';
    }

}
