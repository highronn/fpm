package com.mappy.fpm.batches.tomtom.download.json;

import com.mappy.fpm.batches.tomtom.download.ShapefileExtractor;
import com.mappy.fpm.batches.tomtom.download.json.downloader.*;
import com.mappy.fpm.batches.tomtom.download.json.model.Contents.Content;
import com.mappy.fpm.batches.tomtom.download.json.model.Families.Family;
import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import com.mappy.fpm.batches.tomtom.download.json.model.Releases.Release;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class MapContentDownloaderTest {

    private MapContentDownloader mapContentDownloader;

    private final FamiliesDownloader familiesDownloader = mock(FamiliesDownloader.class);
    private final ProductsDownloader productsDownloader = mock(ProductsDownloader.class);
    private final ReleaseDownloader releaseDownloader = mock(ReleaseDownloader.class);
    private final ContentDownloader contentDownloader = mock(ContentDownloader.class);
    private final DirectUrlDownloader directUrlDownloader = mock(DirectUrlDownloader.class);
    private final ArchiveDownloader archiveDownloader = mock(ArchiveDownloader.class);
    private final ShapefileExtractor shapefileExtractor = mock(ShapefileExtractor.class);
    private final File outputDirectory = mock(File.class);

    @Before
    public void setUp(){
        when(familiesDownloader.get()).thenReturn(newArrayList(new Family("abb1", "loc1"), new Family("abb2", "loc2")).stream());

        when(productsDownloader.apply(new Family("abb1", "loc1"))).thenReturn(newArrayList(new Product("prod1","loc3"), new Product("prod2", "loc4")).stream());
        when(productsDownloader.apply(new Family("abb2", "loc2"))).thenReturn(newArrayList(new Product("prod3","loc5")).stream());

        when(releaseDownloader.apply(new Product("prod1","loc3"))).thenReturn(newArrayList(new Release("2016_09", "loc6")).stream());
        when(releaseDownloader.apply(new Product("prod2","loc4"))).thenReturn(newArrayList(new Release("2016_09", "loc7")).stream());
        when(releaseDownloader.apply(new Product("prod3","loc5"))).thenReturn(newArrayList(new Release("2016_09", "loc8")).stream());

        when(contentDownloader.apply(new Release("2016_09", "loc6"))).thenReturn(newArrayList(new Content("content1", "loc9")).stream());
        when(contentDownloader.apply(new Release("2016_09", "loc7"))).thenReturn(newArrayList(new Content("content2", "loc10"), new Content("content3", "loc11")).stream());
        when(contentDownloader.apply(new Release("2016_09", "loc8"))).thenReturn(newArrayList(new Content("content4", "loc12")).stream());

        when(directUrlDownloader.apply(new Content("content1", "loc9"))).thenReturn(new Content("content1", "locd9"));
        when(directUrlDownloader.apply(new Content("content2", "loc10"))).thenReturn(new Content("content2", "locd10"));
        when(directUrlDownloader.apply(new Content("content3", "loc11"))).thenReturn(new Content("content3", "locd11"));
        when(directUrlDownloader.apply(new Content("content4", "loc12"))).thenReturn(new Content("content4", "locd12"));

        when(archiveDownloader.apply(new Content("content1", "locd9"))).thenReturn(mock(File.class));
        when(archiveDownloader.apply(new Content("content2", "locd10"))).thenReturn(mock(File.class));
        when(archiveDownloader.apply(new Content("content3", "locd11"))).thenReturn(mock(File.class));
        when(archiveDownloader.apply(new Content("content4", "locd12"))).thenReturn(mock(File.class));

        mapContentDownloader = new MapContentDownloader(familiesDownloader, productsDownloader, releaseDownloader, contentDownloader, directUrlDownloader, archiveDownloader, shapefileExtractor, outputDirectory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_illegal_argument_exception_when_call_with_less_than_3_arguments() {
        MapContentDownloader.main(new String[]{"k", "h"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_illegal_argument_exception_when_call_with_more_than_3_arguments() {
        MapContentDownloader.main(new String[]{"k", "h", "l", "m"});
    }

    @Test
    public void should_download_archives(){
        mapContentDownloader.run();

        verify(familiesDownloader).get();
        verify(productsDownloader, times(2)).apply(any(Family.class));
        verify(productsDownloader).apply(new Family("abb1", "loc1"));
        verify(productsDownloader).apply(new Family("abb2", "loc2"));
        verify(releaseDownloader, times(3)).apply(any(Product.class));
        verify(releaseDownloader).apply(new Product("prod1","loc3"));
        verify(releaseDownloader).apply(new Product("prod2","loc4"));
        verify(releaseDownloader).apply(new Product("prod3","loc5"));
        verify(contentDownloader, times(3)).apply(any(Release.class));
        verify(contentDownloader).apply(new Release("2016_09", "loc6"));
        verify(contentDownloader).apply(new Release("2016_09", "loc7"));
        verify(contentDownloader).apply(new Release("2016_09", "loc8"));
        verify(directUrlDownloader, times(4)).apply(any(Content.class));
        verify(directUrlDownloader).apply(new Content("content1", "loc9"));
        verify(directUrlDownloader).apply(new Content("content2", "loc10"));
        verify(directUrlDownloader).apply(new Content("content3", "loc11"));
        verify(directUrlDownloader).apply(new Content("content4", "loc12"));
        verify(archiveDownloader, times(4)).apply(any(Content.class));
        verify(archiveDownloader).apply(new Content("content1", "locd9"));
        verify(archiveDownloader).apply(new Content("content2", "locd10"));
        verify(archiveDownloader).apply(new Content("content3", "locd11"));
        verify(archiveDownloader).apply(new Content("content4", "locd12"));
        verify(shapefileExtractor, times(4)).decompress(eq(outputDirectory), any(File.class));
    }
}