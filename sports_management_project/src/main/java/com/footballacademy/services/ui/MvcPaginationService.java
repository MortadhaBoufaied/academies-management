package com.footballacademy.services.ui;

import com.footballacademy.config.AppUiProperties;
import com.footballacademy.util.PagedResult;
import com.footballacademy.util.PaginationUtil;
import com.footballacademy.util.ViewPagination;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MvcPaginationService {

    private final AppUiProperties appUiProperties;

    public MvcPaginationService(AppUiProperties appUiProperties) {
        this.appUiProperties = appUiProperties;
    }

    // =====================================================
    // === PUBLIC API
    // =====================================================

    public <T> ViewPagination<T> paginate(
            List<T> items,
            Integer page,
            HttpServletRequest request
    ) {
        return paginate(items, page, request, "page");
    }

    public <T> ViewPagination<T> paginate(
            List<T> items,
            Integer page,
            HttpServletRequest request,
            String pageParameterName
    ) {
        int resolvedPage = page != null ? page : 0;
        int pageSize = appUiProperties.getPagination().getItemsPerPage();

        PagedResult<T> pagedResult =
                PaginationUtil.paginate(items, resolvedPage, pageSize);

        long totalItems = pagedResult.totalItems();

        int fromItem = totalItems == 0
                ? 0
                : (pagedResult.page() * pagedResult.size()) + 1;

        int toItem = totalItems == 0
                ? 0
                : fromItem + pagedResult.items().size() - 1;

        boolean hasPrevious = pagedResult.page() > 0;
        boolean hasNext = pagedResult.hasNext();

        String prevHref = hasPrevious
                ? buildHref(request, pageParameterName, pagedResult.page() - 1)
                : null;

        String nextHref = hasNext
                ? buildHref(request, pageParameterName, pagedResult.page() + 1)
                : null;

        List<ViewPagination.PageLink> pageLinks =
                buildPageLinks(
                        request,
                        pageParameterName,
                        pagedResult.page(),
                        pagedResult.totalPages()
                );

        return new ViewPagination<>(
                pagedResult.items(),
                pagedResult.page(),
                pagedResult.totalPages(),
                pagedResult.totalItems(),
                pagedResult.size(),
                fromItem,
                toItem,
                hasPrevious,
                hasNext,
                prevHref,
                nextHref,
                pageLinks
        );
    }

    // =====================================================
    // === PAGE LINKS
    // =====================================================

    private List<ViewPagination.PageLink> buildPageLinks(
            HttpServletRequest request,
            String pageParameterName,
            int currentPage,
            int totalPages
    ) {
        if (totalPages <= 0) {
            return List.of();
        }

        int maxLinks =
                Math.max(1, appUiProperties.getPagination().getMaxPageLinks());

        int halfWindow = maxLinks / 2;

        int start = Math.max(0, currentPage - halfWindow);
        int end = Math.min(totalPages - 1, start + maxLinks - 1);
        start = Math.max(0, end - maxLinks + 1);

        List<ViewPagination.PageLink> links = new ArrayList<>();

        for (int pageIndex = start; pageIndex <= end; pageIndex++) {
            links.add(
                    new ViewPagination.PageLink(
                            String.valueOf(pageIndex + 1),
                            buildHref(request, pageParameterName, pageIndex),
                            pageIndex == currentPage
                    )
            );
        }

        return links;
    }

    // =====================================================
    // === URL BUILDER
    // =====================================================

    private String buildHref(
            HttpServletRequest request,
            String pageParameterName,
            int page
    ) {
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromPath(request.getRequestURI());

        for (Map.Entry<String, String[]> entry :
                request.getParameterMap().entrySet()) {

            String key = entry.getKey();

            if (pageParameterName.equals(key)) {
                continue;
            }

            for (String value : entry.getValue()) {
                if (value != null && !value.isBlank()) {
                    builder.queryParam(key, value);
                }
            }
        }

        builder.queryParam(pageParameterName, page);

        return builder.build().encode().toUriString();
    }
}
