```bash
/stock-manager (main)
$ ls -R src/
src/:
controller  model  util  view
oller.java
e.xml       stock-manager-light-theme.xml
lib                stock-manager.db   stock-manager-dark-purple-theme.xml      stock-manager-solarized-dark-theme.xml
README.md          stock-manager.jar  stock-manager-dark-theme.xml             tickets
reports            stock-manager.xml  stock-manager-dracula-theme.xml

/stock-manager (main)
$ ls -R resources/
resources/:
Messages_ar.properties  Messages_en.properties  samples.sql  schema.sql

$ ls src/util/
ArabicFontHelper.java  DataUtil.java  DBConnection.java  LocaleManager.java  PasswordUtil.java  PDFGenerator.java Messages.java
```

## Implementation for the internationalizations

1. First, create the `LocaleManager` class in the util package to manage language settings.

2. Create a "resources" folder in your project's source directory if it doesn't exist already.

3. Create the following files:
   - `resources/Messages_en.properties` - English strings
   - `resources/Messages_ar.properties` - Arabic translations

4. the `ArabicFontHelper` class in the util package for better Arabic text rendering.

5. Replace your current `view` with the localized version provided.

6. For Arabic fonts, download the Amiri font (open source) and place it in `resources/fonts/Amiri-Regular.ttf`.

7. Make similar changes to other view classes in your application, following the same pattern of using resource bundles.

## Additional Notes for Complete Arabization

1. **Text field directionality**: For proper display of Arabic text in text fields, you may need to set the component orientation:
   ```java
   textField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
   ```

2. **Table columns order**: You might want to reverse the order of table columns for RTL display.

3. **Custom components**: Any custom components will need explicit RTL support.

4. **Font considerations**: Arabic script requires specific fonts that support Arabic characters. The `ArabicFontHelper` helps with this.

5. **Apply similar localization pattern** to all other view classes in your application.

This implementation provides a solid foundation for a fully Arabized stock management system. You'll need to follow the same pattern for the other view classes in your application to complete the full UI Arabization.




---


## prompt for internationalizations


# Messages_en.properties
```bash
....
# Category specific
categories.title=Categories
categories.searchTitle=Search Categories
categories.button.add=Add Category
categories.column.name=Name
categories.column.description=Description
categories.error.inUse=This category cannot be deleted because it is in use by one or more products.
categories.error.nameRequired=Name is a required field.
categories.error.save=Error saving category. Please check your inputs.
categories.error.delete=Error deleting category.
categories.error.selectToDelete=Please select a category to delete.
categories.confirm.delete=Are you sure you want to delete the category: {0}?
categories.success.saved=Category saved successfully.
categories.success.deleted=Category deleted successfully.
....
```

# Messages_ar.properties
```bash
....
# Category specific
categories.title=الفئات
categories.searchTitle=بحث الفئات
categories.button.add=إضافة فئة
categories.column.name=الاسم
categories.column.description=الوصف
categories.error.inUse=لا يمكن حذف هذه الفئة لأنها قيد الاستخدام بواسطة منتج واحد أو أكثر.
categories.error.nameRequired=حقل الاسم مطلوب.
categories.error.save=خطأ في حفظ الفئة. يرجى التحقق من المدخلات.
categories.error.delete=خطأ في حذف الفئة.
categories.error.selectToDelete=الرجاء تحديد فئة للحذف.
categories.confirm.delete=هل أنت متأكد من أنك تريد حذف الفئة: {0}؟
categories.success.saved=تم حفظ الفئة بنجاح.
categories.success.deleted=تم حذف الفئة بنجاح.
....
```
continue the internationalization of the adjustment view in the same manner as the category view, and give me the adjustment specific properties
in the same manner as the Category view remake the adjustment view