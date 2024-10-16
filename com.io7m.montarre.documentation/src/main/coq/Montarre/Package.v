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

Inductive ModuleT :=
  Module : FileNameT -> HashT -> ModuleT.

Inductive PlatformModuleT :=
  PlatformModule : FileNameT -> HashT -> PlatformT -> PlatformModuleT.

Inductive ResourceT :=
  Resource : FileNameT -> HashT -> ResourceRoleT -> CaptionT -> ResourceT.

Inductive ItemT :=
  | ItemModule         : ModuleT         -> ItemT
  | ItemPlatformModule : PlatformModuleT -> ItemT
  | ItemResource       : ResourceT       -> ItemT
  .

Definition itemFileName (i : ItemT) : FileNameT :=
  match i with
  | ItemModule         (Module f _)           => f
  | ItemPlatformModule (PlatformModule f _ _) => f
  | ItemResource       (Resource f _ _ _)     => f
  end.

Definition itemHash (i : ItemT) : HashT :=
  match i with
  | ItemModule         (Module _ h)           => h
  | ItemPlatformModule (PlatformModule _ h _) => h
  | ItemResource       (Resource _ h _ _)     => h
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

