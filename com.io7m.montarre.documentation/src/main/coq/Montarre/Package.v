Require Import Coq.Unicode.Utf8_core.
Require Import Coq.Strings.String.
Require Import Coq.Strings.Ascii.
Require Import Coq.FSets.FMapInterface.
Require Import Coq.FSets.FMapWeakList.
Require Import Coq.FSets.FMapFacts.
Require Import Coq.Structures.Equalities.

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

Inductive ApplicationKindT :=
  | CONSOLE
  | GRAPHICAL
  .

Definition CategoryT :=
  string.

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

Inductive JavaInfoT := JavaInfo {
  requiredJDKVersion : nat;
  mainModule         : string
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

Definition VendorNameT :=
  string.

Definition VendorIDT :=
  string.

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

Definition VersionDateT :=
  string.

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
  string.

Definition ShortNameT :=
  string.

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

