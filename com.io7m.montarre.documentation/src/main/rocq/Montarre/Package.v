(*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *)

From Stdlib Require Import Strings.String.
From Stdlib Require Import Strings.Ascii.
From Stdlib Require Import FSets.FMapInterface.
From Stdlib Require Import FSets.FMapWeakList.
From Stdlib Require Import FSets.FMapFacts.
From Stdlib Require Import Structures.Equalities.

Require Import com.io7m.entomos.Binary.

Import ListNotations.
Local Open Scope string_scope.

(** A mini decidable type module to instantiate maps. *)
Module StringMiniDec : MiniDecidableType
  with Definition t := string.

  Definition t        := string.
  Definition eq       := @Logic.eq t.
  Definition eq_refl  := @Logic.eq_refl t.
  Definition eq_sym   := @Logic.eq_sym t.
  Definition eq_trans := @Logic.eq_trans t.

  Theorem eq_dec : forall x y : t, {eq x y} + {~ eq x y}.
  Proof. apply string_dec. Qed.
End StringMiniDec.

(** A usual decidable type module to instantiate maps. *)
Module StringDec <: UsualDecidableType
  with Definition t := string
  with Definition eq := @Logic.eq string
:= Make_UDT StringMiniDec.

(** A Maps module with string keys. *)
Module StringMaps : FMapInterface.WS
  with Definition E.t  := string
  with Definition E.eq := @Logic.eq string
:= FMapWeakList.Make StringDec.

(** The opaque, abstract type of regular expressions. *)
Parameter RegularExpressionT : string -> Set.

(** For the sake of specification simplicity, we assume that all
    strings are valid regular expressions. *)
Parameter regex : forall (s : string), RegularExpressionT s.

(** A proposition that states that a given regular expression
    matches a given string. *)
Parameter matches : forall (s : string) {e : string},
  RegularExpressionT e -> Prop.

(** Whether a string matches a regular expression is decidable. *)
Parameter matchesDecidable : forall 
  (s : string)
  {e : string}
  (r : RegularExpressionT e),
    {matches s r}+{~matches s r}.

Definition lanarkDottedString :=
  forall (s : string),
    matches s (regex "([a-z][a-z0-9_-]{0,63})(\.[a-z][a-z0-9_-]{0,62}){0,15}").

Inductive ApplicationKindT :=
  | CONSOLE
  | GRAPHICAL
  .

Inductive CategoryT :=
  Category : forall (s : string),
    matches s (regex "[A-Z][A-Za-z0-9_-]{0,128}")
      -> CategoryT.

Definition LicenseT :=
  string.

Definition CopyrightT :=
  string.

Inductive CopyingT := Copying {
  license   : LicenseT;
  copyright : CopyrightT
}.

Definition LanguageT :=
  string.

(** A Maps module with language keys. *)
Module LanguageMaps : FMapInterface.WS
  with Definition E.t  := LanguageT
  with Definition E.eq := @Logic.eq LanguageT
:= FMapWeakList.Make StringDec.

Inductive TranslatedTextT := TranslatedText {
  language     : LanguageT;
  text         : string;
  translations : LanguageMaps.t string
}.

Definition DescriptionT :=
  TranslatedTextT.

Inductive JavaImageT :=
    JDK
  | JRE.

Inductive JavaInfoT := JavaInfo {
  requiredJDKVersion : nat;
  mainModule         : string;
  extraOptions       : list string;
  enableNativeAccess : list string;
  imageType          : JavaImageT
}.

Inductive LinkRoleT :=
  | ISSUES
  | HOME_PAGE
  | DONATION
  | CONTACT
  | FAQ
  | TRANSLATE
  | CONTRIBUTE
  | SCM
  .

Inductive LinkT := Link {
  linkRole   : LinkRoleT;
  linkTarget : string
}.

Inductive VendorNameT :=
  VendorName : forall (s : string),
    matches s (regex "[a-zA-Z][A-Za-z0-9_-]{0,128}")
      -> VendorNameT.

Definition VendorIDT :=
  lanarkDottedString.

Inductive VendorT := Vendor {
  vendorId   : VendorIDT;
  vendorName : VendorNameT
}.

Inductive VersionNumberT := VersionNumber {
  major     : nat;
  minor     : nat;
  patch     : nat;
  qualifier : option string
}.

Inductive VersionDateT :=
  VersionDate : forall (s : string),
    matches s (regex "[0-9]{4}-[0-9]{2}-[0-9]{2}")
      -> VersionDateT.

Inductive VersionT := Version {
  versionNumber : VersionNumberT;
  versionDate   : VersionDateT
}.

Definition ParagraphT :=
  string.

Definition FeatureT :=
  string.

Inductive LongDescriptionT := LongDescription {
  descriptionLanguage : LanguageT;
  descriptions        : list ParagraphT;
  features            : list FeatureT
}.

Inductive FlatpakRuntimeRoleT :=
  | SDK
  | PLATFORM
  .

Definition FlatpakPermissionT :=
  string.

Inductive FlatpakRuntimeT := FlatpakRuntime {
  flatpakRuntimeName    : string;
  flatpakRuntimeVersion : string;
  flatpakRuntimeRole    : FlatpakRuntimeRoleT
}.

Inductive FlatpakT := Flatpak {
  flatpakPermissions : list FlatpakPermissionT;
  flatpakRuntimes    : list FlatpakRuntimeT
}.

Definition PackageNameT :=
  lanarkDottedString.

Inductive ShortNameT :=
  ShortName : forall (s : string),
    matches s (regex "[a-z][a-z0-9_-]{0,128}")
      -> ShortNameT.

Definition HumanNameT :=
  string.

Inductive NamesT := Names {
  namePackage : PackageNameT;
  nameShort   : ShortNameT;
  nameHuman   : HumanNameT
}.

Inductive MetadataT := Metadata {
  metaApplicationKind  : ApplicationKindT;
  metaCategories       : list CategoryT;
  metaCopying          : CopyingT;
  metaDescription      : DescriptionT;
  metaFlatpak          : FlatpakT;
  metaJavaInfo         : JavaInfoT;
  metaLinks            : list LinkT;
  metaLongDescriptions : LanguageMaps.t LongDescriptionT;
  metaNames            : NamesT;
  metaVendor           : VendorT;
  metaVersion          : VersionT
}.

Definition HashAlgorithmT :=
  string.

Inductive HashValueT :=
  HashValue : forall (s : string),
    matches s (regex "[a-f0-9]{2,256}")
      -> HashValueT.

Inductive HashT := Hash {
  hashAlgorithm : HashAlgorithmT;
  hashValue     : HashValueT
}.

Inductive ArchitectureNameT :=
  ArchitectureName : forall (s : string),
    matches s (regex "[a-z][a-z0-9_-]{0,32}")
      -> ArchitectureNameT.

Inductive OperatingSystemNameT :=
  OperatingSystemName : forall (s : string),
    matches s (regex "[a-z][a-z0-9_-]{0,32}")
      -> OperatingSystemNameT.

Inductive PlatformT := Platform {
  platformArch : ArchitectureNameT;
  platformOS   : OperatingSystemNameT
}.

(** A function that produces an uppercase version of the given string. *)
Parameter uppercaseOf : string -> string.

Definition uppercaseSame (s t : string) :=
  uppercaseOf s = uppercaseOf t.

Inductive FileNameT :=
  FileName : forall (s : string),
    matches s (regex "([\p{L}\p{N}_\-.+]+)(/[\p{L}\p{N}_\-.+]+)*")
      -> FileNameT.

Definition fileNameString (i : FileNameT) : string :=
  match i with
  | FileName s _ => s
  end.

(** A proposition that states that two file names are the same if their
    uppercase transformations are the same. *)
Definition fileNamesSame (s t : FileNameT) :=
  match s, t with
  | FileName fs _, FileName ft _ => uppercaseSame fs ft
  end.

Inductive ResourceRoleT :=
  | BOM
  | LICENSE
  | ICO_WINDOWS
  | ICON_16
  | ICON_24
  | ICON_32
  | ICON_48
  | ICON_64
  | ICON_128
  | ICON_256
  | ICON_512
  | ICON_SVG
  | SCREENSHOT
  .

Definition CaptionT :=
  TranslatedTextT.

Inductive ModuleT := Module {
  mFile : FileNameT;
  mHash : HashT;
  mSize : nat
}.

Inductive PlatformModuleT := PlatformModule {
  pmFile     : FileNameT;
  pmHash     : HashT;
  pmPlatform : PlatformT;
  pmSize     : nat
}.

Inductive ResourceT := Resource {
  rFile    : FileNameT;
  rHash    : HashT;
  rRole    : ResourceRoleT;
  rCaption : CaptionT;
  rSize    : nat
}.

Inductive ItemT :=
  | ItemModule         : ModuleT         -> ItemT
  | ItemPlatformModule : PlatformModuleT -> ItemT
  | ItemResource       : ResourceT       -> ItemT
  .

Definition itemFileName (i : ItemT) : FileNameT :=
  match i with
  | ItemModule         x => mFile x
  | ItemPlatformModule x => pmFile x
  | ItemResource       x => rFile x
  end.

Definition itemHash (i : ItemT) : HashT :=
  match i with
  | ItemModule         x => mHash x
  | ItemPlatformModule x => pmHash x
  | ItemResource       x => rHash x
  end.

Definition itemSize (i : ItemT) : nat :=
  match i with
  | ItemModule         x => mSize x
  | ItemPlatformModule x => pmSize x
  | ItemResource       x => rSize x
  end.

Inductive ManifestT := Manifest {
  manifestItems : list ItemT
}.

Definition manifesItemsFilenamesUnique :=
  forall (m : ManifestT),
    forall (i0 : ItemT),
      In i0 (manifestItems m)
        -> ~(exists i1 : ItemT,
               (In i1 (manifestItems m))
            /\ (i0 <> i1)
            /\ (fileNamesSame (itemFileName i0) (itemFileName i1))).

(** A function that produces an XML serialization of the given manifest. *)
Parameter xmlSerializationOf : ManifestT -> string.

(** The file header. *)
Definition binaryExpFileHeader : binaryExp :=
  BiRecord [
    ("id",           u64 0x894D54500D0A1A0A);
    ("versionMajor", u32 1);
    ("versionMinor", u32 0)
  ].

(** The MTP_END! section. *)
Definition binaryEndSection : binaryExp :=
  BiRecord [
    ("id",   u64 0x4D54505F454E4421);
    ("size", u64 0)
  ].

(** The MTP_MANI section. *)
Definition binaryManifestSection (m : ManifestT) : binaryExp := 
  let text := utf8 (xmlSerializationOf m) in
    BiRecord [
      ("id",   u64 0x4D54505F4D414E49);
      ("size", u64 (binarySize text));
      ("text", text)
    ].

(** The MTP_FILE section. *)
Definition binaryFileSection (i : ItemT) : binaryExp := 
  let nameText  := utf8 (uppercaseOf (fileNameString (itemFileName i))) in
  let nameSize  := binarySize nameText in
  let fileSize  := itemSize i in
  let totalSize := 4 + nameSize + fileSize in
    BiRecord [
      ("id",         u64 0x4D54505F46494C45);
      ("size",       u64 totalSize);
      ("nameLength", u32 nameSize);
      ("name",       nameText);
      ("data",       BiReserve fileSize)
    ].

