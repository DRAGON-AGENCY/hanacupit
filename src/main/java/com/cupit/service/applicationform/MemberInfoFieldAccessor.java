package com.cupit.service.applicationform;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.cupit.model.MemberInfo;

/**
 * MemberInfoの物理名(スネークケース) -> ゲッターの対応表。
 * 各決済会社所定申込フォーム作成のフィールドマッピング（データ駆動）が、
 * 物理名の文字列だけから値を解決できるようにするための自動生成クラス。
 */
public final class MemberInfoFieldAccessor {

    private static final Map<String, Function<MemberInfo, Object>> MAP = new HashMap<>();

    static {
        MAP.put("account_holder", MemberInfo::getAccountHolder);
        MAP.put("account_holder_kana", MemberInfo::getAccountHolderKana);
        MAP.put("addr_block", MemberInfo::getAddrBlock);
        MAP.put("addr_block_kana", MemberInfo::getAddrBlockKana);
        MAP.put("addr_building", MemberInfo::getAddrBuilding);
        MAP.put("addr_building_kana", MemberInfo::getAddrBuildingKana);
        MAP.put("addr_city", MemberInfo::getAddrCity);
        MAP.put("addr_city_kana", MemberInfo::getAddrCityKana);
        MAP.put("addr_pref", MemberInfo::getAddrPref);
        MAP.put("addr_pref_kana", MemberInfo::getAddrPrefKana);
        MAP.put("addr_tel", MemberInfo::getAddrTel);
        MAP.put("addr_town", MemberInfo::getAddrTown);
        MAP.put("addr_town_kana", MemberInfo::getAddrTownKana);
        MAP.put("addr_zip", MemberInfo::getAddrZip);
        MAP.put("app_industry_1", MemberInfo::getAppIndustry1);
        MAP.put("app_industry_2", MemberInfo::getAppIndustry2);
        MAP.put("app_industry_3", MemberInfo::getAppIndustry3);
        MAP.put("corp_block", MemberInfo::getCorpBlock);
        MAP.put("corp_block_kana", MemberInfo::getCorpBlockKana);
        MAP.put("corp_building", MemberInfo::getCorpBuilding);
        MAP.put("corp_building_kana", MemberInfo::getCorpBuildingKana);
        MAP.put("corp_city", MemberInfo::getCorpCity);
        MAP.put("corp_city_kana", MemberInfo::getCorpCityKana);
        MAP.put("corp_legal_form", MemberInfo::getCorpLegalForm);
        MAP.put("corp_legal_form_kana", MemberInfo::getCorpLegalFormKana);
        MAP.put("corp_name", MemberInfo::getCorpName);
        MAP.put("corp_name_kana", MemberInfo::getCorpNameKana);
        MAP.put("corp_pref", MemberInfo::getCorpPref);
        MAP.put("corp_pref_kana", MemberInfo::getCorpPrefKana);
        MAP.put("corp_town", MemberInfo::getCorpTown);
        MAP.put("corp_town_kana", MemberInfo::getCorpTownKana);
        MAP.put("corp_zip", MemberInfo::getCorpZip);
        MAP.put("handling_items", MemberInfo::getHandlingItems);
        MAP.put("hcp_town_url", MemberInfo::getHcpTownUrl);
        MAP.put("mgmt_type", MemberInfo::getMgmtType);
        MAP.put("office_contact_email", MemberInfo::getOfficeContactEmail);
        MAP.put("order_delivery_tel", MemberInfo::getOrderDeliveryTel);
        MAP.put("parent_founded_date", MemberInfo::getParentFoundedDate);
        MAP.put("rep_birth", MemberInfo::getRepBirth);
        MAP.put("rep_block", MemberInfo::getRepBlock);
        MAP.put("rep_building", MemberInfo::getRepBuilding);
        MAP.put("rep_city", MemberInfo::getRepCity);
        MAP.put("rep_first_name", MemberInfo::getRepFirstName);
        MAP.put("rep_first_name_kana", MemberInfo::getRepFirstNameKana);
        MAP.put("rep_last_name", MemberInfo::getRepLastName);
        MAP.put("rep_last_name_kana", MemberInfo::getRepLastNameKana);
        MAP.put("rep_pref", MemberInfo::getRepPref);
        MAP.put("rep_town", MemberInfo::getRepTown);
        MAP.put("rep_zip", MemberInfo::getRepZip);
        MAP.put("store_annual_sales_yen", MemberInfo::getStoreAnnualSalesYen);
        MAP.put("store_count", MemberInfo::getStoreCount);
        MAP.put("store_founded_date", MemberInfo::getStoreFoundedDate);
        MAP.put("store_name", MemberInfo::getStoreName);
        MAP.put("store_name_kana", MemberInfo::getStoreNameKana);
        MAP.put("store_name_kana_short", MemberInfo::getStoreNameKanaShort);
    }

    private MemberInfoFieldAccessor() {
    }

    public static Object get(MemberInfo source, String physicalName) {
        Function<MemberInfo, Object> getter = MAP.get(physicalName);
        if (getter == null) {
            throw new IllegalArgumentException(
                    "MemberInfoFieldAccessor: 未知の物理名です: " + physicalName);
        }
        return getter.apply(source);
    }

}
