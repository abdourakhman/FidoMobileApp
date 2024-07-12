package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new com.daon.fido.sdk.sample.kt.DataBinderMapperImpl());
  }
}
