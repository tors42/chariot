package chariot.internal.modeladapter;

import module java.base;
import module chariot;

import chariot.model.Broadcast.*;
import chariot.internal.yayson.Parser.YayNode;
import chariot.internal.yayson.Parser.YayObject;

/*
  "photos": {
    "21869359": {
      "small": "https://image.lichess1.org/display?fmt=webp&h=100&op=thumbnail&path=Oq6dPJNKHvtl.webp&w=100&sig=bec324996c5a9add7938a089c18260a6574d60fc",
      "medium": "https://image.lichess1.org/display?fmt=webp&h=500&op=thumbnail&path=Oq6dPJNKHvtl.webp&w=500&sig=8941fcad6bf59a8065a20f38f65323923b28c52f",
      "credit": "Vivian Passig"
    }
  }
*/
/// Adapter for non-harmonious json layout
/// Broadcast(..., Photos photos)
public interface PhotosAdapter {

    static Photos nodeToPhotos(YayNode node) {
        if (node instanceof YayObject yo) {
            List<Photo> photos = yo.value().entrySet().stream()
                .<Photo>mapMulti((entry, mapper) -> {
                    if (nodeToPhoto(entry.getKey(), entry.getValue()) instanceof Photo photo) {
                        mapper.accept(photo);
                    }
                })
            .toList();
            return new Photos(photos);
        }
        return new Photos(List.of());
    }

    static Photo nodeToPhoto(String id, YayNode node) {
        if (node instanceof YayObject yo
            && stringToURI(yo.getString("small")) instanceof URI small
            && stringToURI(yo.getString("medium")) instanceof URI medium
            && yo.getString("credit") instanceof String credit) {
            return new Photo(id, small, medium, credit);
        }
        return null;
    }

    static URI stringToURI(String str) {
        try { return URI.create(str); } catch (Exception _) { return null; }
    }
}
