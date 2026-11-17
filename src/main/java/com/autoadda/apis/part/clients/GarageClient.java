package com.autoadda.apis.part.clients;

import com.autoadda.apis.part.clients.garage.GarageApiResponse;
import com.autoadda.apis.part.clients.garage.GarageBuyerInfo;
import com.autoadda.apis.part.clients.garage.GaragePartContext;
import com.autoadda.apis.part.clients.garage.MarketplaceStep;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * garage-api calls for garages buying parts. The user's X-Auto-Adda-Id is forwarded, so garage-api checks the
 * PARTS_PURCHASE permission itself (403 without it).
 */
@FeignClient(name = "garage-api", url = "${garage.api.url:http://localhost:8020/api/v1}")
public interface GarageClient {

    String HEADER = "X-Auto-Adda-Id";

    @GetMapping("/garages/{garageId}/purchasing-access")
    GarageApiResponse<GarageBuyerInfo> purchasingAccess(@PathVariable("garageId") Integer garageId,
                                                        @RequestHeader(HEADER) String autoAddaId);

    @GetMapping("/part-requests/item/{itemId}/purchase-context")
    GarageApiResponse<GaragePartContext> purchaseContext(@PathVariable("itemId") Integer itemId,
                                                         @RequestHeader(HEADER) String autoAddaId);

    @PostMapping("/part-requests/item/{itemId}/marketplace")
    GarageApiResponse<GaragePartContext> marketplace(@PathVariable("itemId") Integer itemId,
                                                     @RequestBody MarketplaceStep step,
                                                     @RequestHeader(HEADER) String autoAddaId);
}
