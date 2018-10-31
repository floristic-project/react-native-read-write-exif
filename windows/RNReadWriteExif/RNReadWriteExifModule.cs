using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Read.Write.Exif.RNReadWriteExif
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNReadWriteExifModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNReadWriteExifModule"/>.
        /// </summary>
        internal RNReadWriteExifModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNReadWriteExif";
            }
        }
    }
}
